#!/usr/bin/env bash
. /etc/profile

APPNAME="biocyc-pipeline"
APPDIR=/home/rgddata/pipelines/$APPNAME
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`

EMAIL_LIST=mtutaj@mcw.edu
if [ "$SERVER" == "REED" ]; then
  EMAIL_LIST=mtutaj@mcw.edu
fi

cd $APPDIR
java -Dspring.config=$APPDIR/../properties/default_db2.xml \
    -Dlog4j.configurationFile=file://$APPDIR/properties/log4j2.xml \
    -jar lib/${APPNAME}.jar "$@" > $APPDIR/run.log 2>&1

if [ $? -eq 0 ]; then
  mailx -s "[$SERVER] BioCyc pipeline OK" $EMAIL_LIST < $APPDIR/logs/summary.log
else
  mailx -s "[$SERVER] BioCyc pipeline FAILED" $EMAIL_LIST < $APPDIR/run.log
fi

# incoming BioCyc genes that matched only inactive (withdrawn, retired) RGD genes
INACTIVE_GENES_FILE="$APPDIR/logs/inactive_genes_daily.log"
if [ -s "$INACTIVE_GENES_FILE" ]; then
  mailx -s "[$SERVER] BioCyc pipeline: hits to inactive genes" $EMAIL_LIST < $INACTIVE_GENES_FILE
fi

