#!/bin/bash

# This is a template for creating backup of data and persist them
# to a central backup server. Make sure to adapt this script to your needs.
#
# Please do not change the name of the bakup (bup save -n) because that can
# have strange effects. If you change it you absolutely need to make sure that
# only the same root directories are associated to that name. It is NOT possible
# to backup different root directories to the same backup name.
#
# Documentation: https://wiki.sapsailing.com/wiki/info/landscape/amazon-ec2-backup-strategy
#
# Maintainer: simon.marcel.pamies@sap.com

# If you want to just backup directories follow steps 1-4. If you want to
# backup MongoDB and/or MySQL databases then follow steps 5-8.

# 1: Select directories to backup. Each directory needs to be separated from
# others by a blank. Directories with blanks need to be surrounded by quotes.
BACKUP_DIRECTORIES="/etc /var/log/mongodb"

# 2: Prefix for backup - set that to the hostname of your server or any other
# name that describes your server. No blanks or special characters allowed.
PREFIX="database"

# 3: Directory for temporary files. If you do not want to backup MySQL or MongoBD
# then you can just leave it to /tmp.
TARGET_DIR=/tmp

# 4: Include any patterns that should match files to be ignored. The parameter
# is a regular expression. You can use multiple --exclude-rx= parameters.
BUP_IGNORES='--exclude-rx=/war/$'

# **************************************
# If you do not want any MySQL or MongoDB backup then you're done.
# If not then continue with the following steps.
# **************************************

# 5: Enable MongoDB backup by setting this variable to 1
ENABLE_MONGODB_BACKUP=0

# 6: Provide MongoDB instances to backup. Format is <port>:<database name>
MONGODB_DATABASES="10202:winddb 10201:winddb"

# No need to change the following values in most cases
MONGOEXPORT_CMD="/opt/mongodb-linux-x86_64-1.8.1/bin/mongoexport"
MONGODB_COLLECTIONS_NO_WIND="EVENTS LEADERBOARD_GROUPS LEADERBOARDS RACE_LOGS RACES_MESSAGES REGATTA_FOR_RACE_ID REGATTAS TRACTRAC_CONFIGURATIONS COMMAND_MESSAGES COMPETITORS CONFIGURATIONS LAST_MESSAGE_COUNT VIDEOS"
MONGODB_COLLECTIONS_WIND="WIND_TRACKS"

# 7: Enable MySQL backup by setting this variable to 1
ENABLE_MYSQL_BACKUP=0

# 8: Space separated list of mysql databases to backup
MYSQL_DATABASES="bugs mysql"

# 9: Provide the username and password for MySQL
MYSQLEXPORT_CMD="mysqldump -u root --password=xxxx"

# **************************************
# You're done.
# **************************************

# Configuration for external backup server - needs a ssh key
BACKUP_SERVER="backup@172.31.25.136"

# Set date for this backup - this makes it possible to restore
# files from different branches (named backups)
BACKUP_DATE=`date +%s`

# Aliases
BUP_CMD="/opt/bup/bup"
BUP_ADDITIONAL="-r $BACKUP_SERVER:/home/backup/$PREFIX"
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
# Start of database specific backup
# **************************************

if [ $ENABLE_MONGODB_BACKUP -eq 1 ]; then
    # Export MongoDB instances with wind upon request (weekly parameter provided)
    if [ "$PARAM" = "weekly" ]; then
        for MONGO_CONFIG in $MONGODB_DATABASES; do
            PARTS=(${MONGO_CONFIG//:/ })
            mkdir $TARGET_DIR/mongodb-${PARTS[0]}-wind
            for COLLECTION in $MONGODB_COLLECTIONS_WIND; do
                echo "$MONGOEXPORT_CMD --port ${PARTS[0]} -d ${PARTS[1]} -c $COLLECTION > $TARGET_DIR/mongodb-${PARTS[0]}-wind/$COLLECTION.json"
                $MONGOEXPORT_CMD --port ${PARTS[0]} -d ${PARTS[1]} -c $COLLECTION > $TARGET_DIR/mongodb-${PARTS[0]}-wind/$COLLECTION.json
            done
            done
    fi

    # Export MongoDB instances without wind
    for MONGO_CONFIG in $MONGODB_DATABASES; do
        PARTS=(${MONGO_CONFIG//:/ })
        mkdir $TARGET_DIR/mongodb-${PARTS[0]}
        for COLLECTION in $MONGODB_COLLECTIONS_NO_WIND; do
            echo "$MONGOEXPORT_CMD --port ${PARTS[0]} -d ${PARTS[1]} -c $COLLECTION > $TARGET_DIR/mongodb-${PARTS[0]}/$COLLECTION.json"
            $MONGOEXPORT_CMD --port ${PARTS[0]} -d ${PARTS[1]} -c $COLLECTION > $TARGET_DIR/mongodb-${PARTS[0]}/$COLLECTION.json
        done
    done

    # Backup the whole thing now and push to external server
    $BUP_CMD_INDEX $TARGET_DIR
    $BUP_CMD_SAVE -n mongodb-databases $TARGET_DIR/
    find $TARGET_DIR -name *.json | xargs rm -f
fi

if [ $ENABLE_MYSQL_BACKUP -eq 1 ]; then
    # Export MySQL dump for selected databases
    for MYSQL_DB in $MYSQL_DATABASES; do
        mkdir $TARGET_DIR/mysql
        echo "$MYSQLEXPORT_CMD $MYSQL_DB > $TARGET_DIR/mysql/$MYSQL_DB.sql"
        $MYSQLEXPORT_CMD $MYSQL_DB > $TARGET_DIR/mysql/$MYSQL_DB.sql
    done

    # Backup the whole thing now and push to external server
    $BUP_CMD_INDEX $TARGET_DIR
    $BUP_CMD_SAVE -n mysql-databases $TARGET_DIR/mysql
    rm -f $TARGET_DIR/mysql/*.sql
fi

# **************************************
# End of database specific backup
# **************************************
