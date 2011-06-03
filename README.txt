OCLC OAI-PMH Harvester
======================

ABOUT
=====

The original harvester code (http://www.oclc.org/research/activities/past/orprojects//harvester2/harvester2.htm) has been converted into an ant project.


PRE-RUN
=======

Before running, please review the OAI-PHM targets you wish to harvest at the bottom of scripts/harvest.sh.

Specify a target in the form:

do_harvest <http://OAITARGET> <filename.xml> <metadata format>

e.g To harvest Jorum
do_harvest http://resources.jorum.ac.uk/oai/request jorum.xml oai_dc

Note the the filename must include the .xml suffix.

RUNNING
=======

To list available ant targets, run:

ant -projecthelp

Running:

ant do_harvest

will build and start harvesting any target(s) you have specified in harvest.sh

POST-RUN
========

Once the harvest has completed, you should find the results in the harvests directory with a symlink (whatever you called filename.xml) pointing to the most recent harvest.

If you run the harvest tool a number of times, older harvests are moved to the harvests_archive directory where you can delete them at your convenience.

The harvest.sh script can be run independently of ant (as long as the harvester jar has been built) if desired.



