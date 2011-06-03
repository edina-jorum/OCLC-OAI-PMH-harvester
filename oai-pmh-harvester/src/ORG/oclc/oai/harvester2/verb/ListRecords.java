
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

package ORG.oclc.oai.harvester2.verb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * This class represents an ListRecords response on either the server or
 * on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class ListRecords extends HarvesterVerb {
    /**
     * Mock object constructor (for unit testing purposes)
     */
    public ListRecords() {
        super();
    }
    
    /**
     * Client-side ListRecords verb constructor
     *
     * @param baseURL the baseURL of the server to be queried
     * @exception MalformedURLException the baseURL is bad
     * @exception SAXException the xml response is bad
     * @exception IOException an I/O error occurred
     */
    public ListRecords(String baseURL, String from, String until,
            String set, String metadataPrefix)
    throws IOException, ParserConfigurationException, SAXException,
    TransformerException {
        super(getRequestURL(baseURL, from, until, set, metadataPrefix));
    }
    
    /**
     * Client-side ListRecords verb constructor (resumptionToken version)
     * @param baseURL
     * @param resumptionToken
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public ListRecords(String baseURL, String resumptionToken)
    throws IOException, ParserConfigurationException, SAXException,
    TransformerException {
        super(getRequestURL(baseURL, resumptionToken));
    }
    
    /**
     * Added by CG - need to include metatdataPrefix. Client-side ListRecords verb constructor (resumptionToken version)
     * @param baseURL
     * @param resumptionToken
     * @param metatdataPrefix
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public ListRecords(String baseURL, String resumptionToken, String metatdataPrefix)
    throws IOException, ParserConfigurationException, SAXException,
    TransformerException {
        super(getRequestURL(baseURL, resumptionToken, metatdataPrefix));
    }
    
    
    /**
     * Get the oai:resumptionToken from the response
     * 
     * @return the oai:resumptionToken value
     * @throws TransformerException
     * @throws NoSuchFieldException
     */
    public String getResumptionToken()
    throws TransformerException, NoSuchFieldException {
        String schemaLocation = getSchemaLocation();
        if (schemaLocation.indexOf(SCHEMA_LOCATION_V2_0) != -1) {
        	// CG Intralibrary 2.8 and 2.9 (not tested in other versions of intralibrary) place the resumption token in /oai20:OAI-PMH/oai20:resumptionToken
        	//Dspace places it at /oai20:OAI-PMH/oai20:ListRecords/oai20:resumptionToken
        	String resumptionToken = getSingleString("/oai20:OAI-PMH/oai20:ListRecords/oai20:resumptionToken");
        	if (resumptionToken.length()==0){
        		//try alternative xPath
        		resumptionToken = getSingleString("/oai20:OAI-PMH/oai20:resumptionToken");
        	}
        	return resumptionToken;
        } else if (schemaLocation.indexOf(SCHEMA_LOCATION_V1_1_LIST_RECORDS) != -1) {
        	return getSingleString("/oai11_ListRecords:ListRecords/oai11_ListRecords:resumptionToken");
        } else {
                throw new NoSuchFieldException(schemaLocation);
        }
    }
    
    /**
     * Construct the query portion of the http request
     *
     * @return a String containing the query portion of the http request
     */
    private static String getRequestURL(String baseURL, String from,
            String until, String set,
            String metadataPrefix) {
        StringBuffer requestURL =  new StringBuffer(baseURL);
        requestURL.append("?verb=ListRecords");
        if (from != null) requestURL.append("&from=").append(from);
        if (until != null) requestURL.append("&until=").append(until);
        if (set != null) requestURL.append("&set=").append(set);
        requestURL.append("&metadataPrefix=").append(metadataPrefix);
        return requestURL.toString();
    }
    
    /**
     * Construct the query portion of the http request (resumptionToken version)
     * @param baseURL
     * @param resumptionToken
     * @return
     * @throws UnsupportedEncodingException 
     */
    private static String getRequestURL(String baseURL,
            String resumptionToken) throws UnsupportedEncodingException {
        StringBuffer requestURL =  new StringBuffer(baseURL);
        requestURL.append("?verb=ListRecords");
        requestURL.append("&resumptionToken=").append(URLEncoder.encode(resumptionToken, "UTF-8"));
        return requestURL.toString();
    }
    
     private static String getRequestURL(String baseURL,
            String resumptionToken, String metadataPrefix) throws UnsupportedEncodingException {
    	
        StringBuffer requestURL =  new StringBuffer(baseURL);
        requestURL.append("?verb=ListRecords");
        requestURL.append("&resumptionToken=").append(URLEncoder.encode(resumptionToken, "UTF-8"));
        /* 
         *  Commented out for now.  According to the spec at http://www.openarchives.org/OAI/openarchivesprotocol.html, for ListRecords:
         *  resumptionToken: an exclusive argument with a value that is the flow control token returned by a previous ListRecords request that issued an incomplete list.
         *  Intralibrary requires the metadataPrefix in addition to the resumption token, which contravenes the above.
         *  Unfortunately, if you send the metadataPrefix to other repositories along with the resumption token, they will correctly respond
         *  with an error.  So for the time being, code will implement the spec.  If you wish to harvest an Intralibrary repository using
         *  resumption tokens, you will have to alter this method.
         */
        
        // FIXME: This is a hack to get us up and running.  May be problematic if we start harvesting other targets
        // Essentially, for a resumption request, dspace only needs the resumption token, whereas intralibrary needs the
        // resumption token and the metadataPrefix
        // if(metadataPrefix.length()==0 || resumptionToken.contains(metadataPrefix)){
    		// most likely a Dspace resumption request where a metadataPrefix is not 
    		// required and will cause an error if sent (Dspace resumption tokens appear to contain the
        	// metadata prefix, intralibrary resumption tokens do not)
        	return requestURL.toString();
    	//}
        
        //requestURL.append("&metadataPrefix=").append(URLEncoder.encode(metadataPrefix, "UTF-8"));
        //return requestURL.toString();
    }
}
