#!/bin/bash

CURRENT_DIR=`dirname $0`
HARVESTER=`cd "$CURRENT_DIR/.." ; pwd`
HARVEST_DIR=`cd "$HARVESTER"/harvests ; pwd`
HARVEST_ARCHIVE_DIR=`cd "$HARVESTER"/harvests_archive ; pwd`
HARVEST_JAR="$HARVESTER/dist/harvester.jar"
APPEND=true

do_harvest()
{

OAI_TARGET_URL="$1"
HARVEST_FILE_NAME="$2"
METADATA_PREFIX="$3"
echo "Target is $OAI_TARGET_URL"
echo "Target_File is $HARVEST_FILE_NAME"
echo "Metadata_prefix is $METADATA_PREFIX"

if [ -z "$METADATA_PREFIX" ]
then
  echo "No metadata prefix supplied, setting oai_dc as default."
  METADATA_PREFIX="oai_dc"
fi
   

DATESTR=`date '+%d%m%y-%H-%M-%S'`
CURRENT_HARVEST_FILE_NAME="${HARVEST_FILE_NAME}_${DATESTR}"

MV_CMD="mv ${HARVEST_DIR}/${HARVEST_FILE_NAME}_* ${HARVEST_ARCHIVE_DIR}"
HARVEST_CMD="java -jar $HARVEST_JAR $OAI_TARGET_URL -out $HARVEST_DIR/$CURRENT_HARVEST_FILE_NAME -append $APPEND -metadataPrefix $METADATA_PREFIX"
echo "Running $HARVEST_CMD"

echo "******************************************************"
echo "* Moving existing harvest to archive: ${HARVEST_DIR}/${HARVEST_ARCHIVE_DIR}... "
$MV_CMD

echo "* Beginning harvest... "
$HARVEST_CMD
HARVEST_EXIT_CODE=$?

SYM_LINK="${HARVEST_DIR}/${HARVEST_FILE_NAME}"


if [ ${HARVEST_EXIT_CODE} -eq 0 ]; then

	 # Remove symlink if it already exists.
     if [ -h ${SYM_LINK} ]; then
     	rm ${SYM_LINK}
     fi

	ln -s ${HARVEST_DIR}/${CURRENT_HARVEST_FILE_NAME} ${SYM_LINK}

	echo "******************************************************"
	echo "* Harvest from ${OAI_TARGET_URL} completed successfully"
	echo "*"
	echo "* Latest harvest is: ${HARVEST_DIR}/${HARVEST_FILE_NAME}"
	echo "*"
	echo "******************************************************"
		
else
	echo "*"
	echo "* Problem harvesting from ${OAI_TARGET_URL}"
	echo "* Harvest exit code was: ${HARVEST_EXIT_CODE}"
	echo "*"
	echo "******************************************************"
	exit 1
fi

}

# TO HARVEST:
#	do_harvest <http://OAITARGET> <filename.xml> <metadata format>
#	If no metadata format supplied, defaults to oai_dc

#  e.g To harvest Jorum
do_harvest http://resources.jorum.ac.uk/oai/request jorum.xml oai_dc
# Add as many targets as you like - just call do_harvest for each one

exit 0