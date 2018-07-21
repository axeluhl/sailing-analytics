#!/bin/sh

# This is a template for creating backup of data and persist them
# to a central backup server. Make sure to adapt this script to your needs.
#
# Please do not change the name of the bakup (bup save -n) because that can
# have strange effects. If you change it you absolutely need to make sure that
# only the same root directories are associated to that name. It is NOT possible
# to backup different root directories to the same backup name.
#
# Maintainer: simon.marcel.pamies@sap.com
#
# ######################################################################
# CHANGELOG
#
# 2016-09-13 (Steffen Wagner):
#   - extracting all databases from a mongo connection instead of defining
#     the exportable databases manually
#   - made databases excludeable (by regex and name)
#

# Directories to backup
BACKUP_DIRECTORIES="/etc /var/log/mongodb"

# Prefix for backup - set that to the hostname of your server
# Make sure to change this!
PREFIX="database"

# Directory for temporary files
TARGET_DIR=/var/lib/mysql/backup

# Configuration for MongoDB backup
MONGO_CONNECTIONS="localhost:10201 localhost:10202"
MONGOEXPORT_CMD="/opt/mongodb-linux-x86_64-1.8.1/bin/mongoexport"
MONGODB_COLLECTIONS_NO_WIND="ActiveFileStorageService COMMAND_MESSAGES COMPETITORS CONFIGURATIONS EVENTS FileStorageServiceProperties GPS_FIXES GPS_FIXES_METADATA IGTIMI_ACCESS_TOKENS LAST_MESSAGE_COUNT LEADERBOARDS LEADERBOARD_GROUPS LEADERBOARD_GROUP_LINKS_FOR_EVENTS PREFERENCES RACES_MESSAGES RACE_LOGS REGATTAS REGATTA_FOR_RACE_ID REGATTA_LOGS RESULT_URLS SAILING_SERVERS SETTINGS SWISSTIMING_ARCHIVE_CONFIGURATIONS SWISSTIMING_CONFIGURATIONS TRACTRAC_CONFIGURATIONS USERS VIDEOS"
MONGODB_COLLECTIONS_WIND="WIND_TRACKS"
MONGODB_EXCLUDEDB="10201:LOAD-TEST 10201:winddb 10201:live 10201:local 10201:admin 10202:DNSTEST2 10202:imagetest 10202:live 10202:replication-test-master 10202:load-test 10202:admin 10202:dev 10202:dev-2010 10202:BYC-2010 10202:sapsailinganalytics-dev 10202:test"

# Configuration for MySQL
MYSQL_DATABASES="bugs mysql"
MYSQLEXPORT_CMD="mysqldump -u root --password=sailaway"

# Configuration for external backup server - needs a ssh key
BACKUP_SERVER="backup@172.31.25.136"

# Set date for this backup - this makes it possible to restore
# files from different branches (named backups)
BACKUP_DATE=`date +%s`

# Aliases
BUP_CMD="/opt/bup/bup"
BUP_ADDITIONAL="-r $BACKUP_SERVER:/home/backup/$PREFIX"
BUP_IGNORES='--exclude-rx=/war/$'
BUP_CMD_INDEX="$BUP_CMD index $BUP_IGNORES"
BUP_CMD_SAVE="$BUP_CMD save --date=$BACKUP_DATE $BUP_ADDITIONAL"

PARAM=$@

# Make sure to install the right libraries
yum install python-devel fuse-python pyxattr pylibacl perl-Time-HiRes

# We need to set HOME in case it isn't set right - assuming root is calling
export HOME=/root

# Make sure to init local and remote repository
# It is perfectly safe to reinitialize a repository w/o overwriting data
$BUP_CMD init
ssh $BACKUP_SERVER "/opt/bup/bup init -r /home/backup/$PREFIX"

# Backup general directories
for OTHER_DIR in $BACKUP_DIRECTORIES; do
	NORMALIZED_DIR_NAME=${OTHER_DIR//\//-}
	echo "Backing up $OTHER_DIR"
	$BUP_CMD_INDEX $OTHER_DIR
	$BUP_CMD_SAVE -n dir$NORMALIZED_DIR_NAME $OTHER_DIR
	# no need to remove any backup files here
done

# **************************************
# Start of database backups
# **************************************

# Backup all databases from all mongo connections
for MONGO_DB in $MONGO_CONNECTIONS; do
    CONNECTION=(${MONGO_DB//:/ })
    DATABASES_UNFORMATTED=($(mongo --host ${CONNECTION[0]} --port ${CONNECTION[1]} --quiet --eval "JSON.stringify(db.adminCommand('listDatabases'))"))
    DATABASES_FORMATTED=`echo ${DATABASES_UNFORMATTED} | jq -r --arg port ${CONNECTION[1]} '.databases| map($port + ":" + .name)|@csv' | tr -d '"' | tr ',' ' '`
    
    # Remove all explicit excluded databases
    LIST_DATABASES_FORMATTED=( $DATABASES_FORMATTED )
    LIST_MONGODB_EXCLUDEDB=( $MONGODB_EXCLUDEDB ) 
    DATABASES_EXCLUDED=`echo ${LIST_DATABASES_FORMATTED[@]} ${LIST_MONGODB_EXCLUDEDB[@]} | tr ' ' '\n' | sort | uniq -u`

    # Remove all regex excluded databases
    DATABASES_FINAL=`echo ${DATABASES_EXCLUDED} | sed -r 's/[0-9]{5}:replica[0-9]+//gi'`
    
    # Export MongoDB instances with wind upon request (weekly parameter provided)
    if [ "$PARAM" = "weekly" ]; then
        for MONGO_CONFIG in $DATABASES_FINAL; do
            PARTS=(${MONGO_CONFIG//:/ })
            if [ "${PARTS[1]}" != "winddb" ]; then
                mkdir $TARGET_DIR/mongodb-${PARTS[0]}-${PARTS[1]}-wind
                DB_SUFFIX=${PARTS[0]}-${PARTS[1]}-wind
            else
                mkdir $TARGET_DIR/mongodb-${PARTS[0]}-wind
                DB_SUFFIX=${PARTS[0]}-wind
            fi
            for COLLECTION in $MONGODB_COLLECTIONS_WIND; do
                echo "$MONGOEXPORT_CMD --port ${PARTS[0]} -d ${PARTS[1]} -c $COLLECTION > $TARGET_DIR/mongodb-$DB_SUFFIX/$COLLECTION.json"
                $MONGOEXPORT_CMD --port ${PARTS[0]} -d ${PARTS[1]} -c $COLLECTION > $TARGET_DIR/mongodb-$DB_SUFFIX/$COLLECTION.json
            done
        done
    fi

    # Export MongoDB instances without wind
    for MONGO_CONFIG in $DATABASES_FINAL; do
        PARTS=(${MONGO_CONFIG//:/ })
        if [ "${PARTS[1]}" != "winddb" ]; then
            mkdir $TARGET_DIR/mongodb-${PARTS[0]}-${PARTS[1]}
            DB_SUFFIX=${PARTS[0]}-${PARTS[1]}
        else
            mkdir $TARGET_DIR/mongodb-${PARTS[0]}
            DB_SUFFIX=${PARTS[0]}
        fi
        for COLLECTION in $MONGODB_COLLECTIONS_NO_WIND; do
            echo "$MONGOEXPORT_CMD --port ${PARTS[0]} -d ${PARTS[1]} -c $COLLECTION > $TARGET_DIR/mongodb-$DB_SUFFIX/$COLLECTION.json"
            $MONGOEXPORT_CMD --port ${PARTS[0]} -d ${PARTS[1]} -c $COLLECTION > $TARGET_DIR/mongodb-$DB_SUFFIX/$COLLECTION.json
        done
    done
done

# Export MySQL dump for selected databases
for MYSQL_DB in $MYSQL_DATABASES; do
	mkdir $TARGET_DIR/mysql
	echo "$MYSQLEXPORT_CMD $MYSQL_DB > $TARGET_DIR/mysql/$MYSQL_DB.sql"
	$MYSQLEXPORT_CMD $MYSQL_DB > $TARGET_DIR/mysql/$MYSQL_DB.sql
done

# Backup the whole thing now and push to external server
$BUP_CMD_INDEX $TARGET_DIR
$BUP_CMD_SAVE -n databases $TARGET_DIR/

# clean up
find $TARGET_DIR -name *.json | xargs rm -f
rm -f $TARGET_DIR/mysql/*.sql

# **************************************
# End of database specific backup
# **************************************