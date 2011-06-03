/**
 Copyright 2006 OCLC, Online Computer Library Center
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package ORG.oclc.oai.harvester2.app;


import java.io.*;
import java.lang.NoSuchFieldException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import ORG.oclc.oai.harvester2.verb.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RawWrite {
	public static void main(String[] args) {
		OutputStream out = System.out;
		try {
			
			HashMap options = getOptions(args);
			List rootArgs = (List) options.get("rootArgs");
			String baseURL = null;
			if (rootArgs.size() > 0) {
				baseURL = (String) rootArgs.get(0);
			} else {
				throw new IllegalArgumentException();
			}

			String outFileName = (String) options.get("-out");
			
			boolean append = true;
			try {
				append = Boolean.parseBoolean(options.get("-append").toString());
				//System.out.println("append = " + append);
			} catch (Exception e) {
				System.out.println("append not found - default is true");
			}

			String from = (String) options.get("-from");
			String until = (String) options.get("-until");
			String metadataPrefix = (String) options.get("-metadataPrefix");
			if (metadataPrefix == null)
				metadataPrefix = "oai_dc";
			String resumptionToken = (String) options.get("-resumptionToken");
			String setSpec = (String) options.get("-setSpec");

			if (resumptionToken != null) {
				if (outFileName != null)
					out = new FileOutputStream(outFileName, true);
				run(baseURL, resumptionToken, out);
			} else {
				if (outFileName != null) {
					if (append) {
						out = new FileOutputStream(outFileName, append);
						run(baseURL, from, until, metadataPrefix, setSpec, out);
					} else {
						runNoAppend(baseURL, from, until, metadataPrefix, setSpec, out, outFileName, resumptionToken);
					}
				}
			}

			
		} catch (IllegalArgumentException e) {
			System.err
					.println("RawWrite <-from date> <-until date> <-metadataPrefix prefix> <-setSpec setName> <-resumptionToken token> <-out fileName> baseURL <-append true/false>");
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (out != System.out)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			System.exit(0);
		}
	}

	//Added by CG to write results to different files
	public static void runNoAppend(String baseURL, String from, String until, String metadataPrefix, String setSpec,
			OutputStream out, String outFileName, String resumptionToken) throws IOException,
			ParserConfigurationException, SAXException, TransformerException, NoSuchFieldException {

		ListRecords listRecords = new ListRecords(baseURL, from, until, setSpec, metadataPrefix);
		String[] split = outFileName.split(".xml");
		int count = 0;
		while (listRecords != null) {
			count++;
			StringBuffer sb = new StringBuffer(split[0]);
			if (resumptionToken != null) {
				sb.append("_").append(count);
			}
			sb.append(".xml");
			out = new FileOutputStream(sb.toString());
			NodeList errors = listRecords.getErrors();
			if (errors != null && errors.getLength() > 0) {
				System.out.println("Found errors");
				int length = errors.getLength();
				for (int i = 0; i < length; ++i) {
					Node item = errors.item(i);
					System.out.println(item);
				}
				System.out.println("Error record: " + listRecords.toString());
				break;
			}

			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8"));
			out.write("<harvest>\n".getBytes("UTF-8"));
			out.write(listRecords.toString().getBytes("UTF-8"));
			out.write("\n".getBytes("UTF-8"));
			out.write("</harvest>\n".getBytes("UTF-8"));

			resumptionToken = listRecords.getResumptionToken();
			//System.out.println("resumptionToken: " + resumptionToken);
			if (resumptionToken == null || resumptionToken.length() == 0) {
				listRecords = null;
			} else {
				listRecords = new ListRecords(baseURL, resumptionToken, metadataPrefix);
			}

			out.flush();
			out.close();
		}
	}

	public static void run(String baseURL, String resumptionToken, OutputStream out) throws IOException,
			ParserConfigurationException, SAXException, TransformerException, NoSuchFieldException {
		ListRecords listRecords = new ListRecords(baseURL, resumptionToken);
		while (listRecords != null) {
			NodeList errors = listRecords.getErrors();
			if (errors != null && errors.getLength() > 0) {
				System.out.println("Found errors");
				int length = errors.getLength();
				for (int i = 0; i < length; ++i) {
					Node item = errors.item(i);
					System.out.println(item);
				}
				System.out.println("Error record: " + listRecords.toString());
				break;
			}
			out.write(listRecords.toString().getBytes("UTF-8"));
			out.write("\n".getBytes("UTF-8"));
			resumptionToken = listRecords.getResumptionToken();
			//System.out.println("resumptionToken: " + resumptionToken);
			if (resumptionToken == null || resumptionToken.length() == 0) {
				listRecords = null;
			} else {
				listRecords = new ListRecords(baseURL, resumptionToken);
			}
		}
		out.write("</harvest>\n".getBytes("UTF-8"));
	}

	public static void run(String baseURL, String from, String until, String metadataPrefix, String setSpec,
			OutputStream out) throws IOException, ParserConfigurationException, SAXException, TransformerException,
			NoSuchFieldException {
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8"));
		out.write("<harvest>\n".getBytes("UTF-8"));
		out.write(new Identify(baseURL).toString().getBytes("UTF-8"));
		out.write("\n".getBytes("UTF-8"));
		out.write(new ListMetadataFormats(baseURL).toString().getBytes("UTF-8"));
		out.write("\n".getBytes("UTF-8"));
		out.write(new ListSets(baseURL).toString().getBytes("UTF-8"));
		out.write("\n".getBytes("UTF-8"));
		ListRecords listRecords = new ListRecords(baseURL, from, until, setSpec, metadataPrefix);
		while (listRecords != null) {
			NodeList errors = listRecords.getErrors();
			if (errors != null && errors.getLength() > 0) {
				System.out.println("Found errors");
				int length = errors.getLength();
				for (int i = 0; i < length; ++i) {
					Node item = errors.item(i);
					System.out.println(item);
				}
				System.out.println("Error record: " + listRecords.toString());
				break;
			}
 
			out.write(listRecords.toString().getBytes("UTF-8"));
			out.write("\n".getBytes("UTF-8"));
			String resumptionToken = listRecords.getResumptionToken();
			//System.out.println("resumptionToken: " + resumptionToken);
			if (resumptionToken == null || resumptionToken.length() == 0) {
				listRecords = null;
			} else {
				//Modified original code to add metadataPrefix parameter
				listRecords = new ListRecords(baseURL, resumptionToken, metadataPrefix);
			}
		}
		out.write("</harvest>\n".getBytes("UTF-8"));
	}

	private static HashMap getOptions(String[] args) {
		HashMap options = new HashMap();
		ArrayList rootArgs = new ArrayList();
		options.put("rootArgs", rootArgs);

		for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) != '-') {
				rootArgs.add(args[i]);
			} else if (i + 1 < args.length) {
				options.put(args[i], args[++i]);
			} else {
				throw new IllegalArgumentException();
			}
		}
		return options;
	}
}
