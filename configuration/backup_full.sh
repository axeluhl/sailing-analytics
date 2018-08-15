#!/bin/sh

# This is a template for creating backup of data and persist them
# to a central backup server. Make sure to adapt this script to your needs.
#
# Please do not change the name of the bakup (bup save -n) because that can
# have strange effects. If you change it you absolutely need to make sure that
# only the same root directories are associated to that name. It is NOT possible
# to backup different root directories to the same backup name.
#
# Maintainer: simon.marcel.pamies@sap.com, axel.uhl@sap.com

# Directories to backup
BACKUP_DIRECTORIES="/etc /var/log/mongodb"

# Prefix for backup - set that to the hostname of your server
# Make sure to change this!
PREFIX="database"

# Directory for temporary files
TARGET_DIR=/var/lib/mysql/backup

# Configuration for MongoDB backup
MONGO_PORTS="10202 10201"
MONGO_INSTALLATION=/opt/mongodb-linux-x86_64-2.6.7
MONGO_CMD=$MONGO_INSTALLATION/bin/mongo
MONGOEXPORT_CMD=$MONGO_INSTALLATION/bin/mongoexport
MONGODB_COLLECTION_WIND="WIND_TRACKS"
MONGODB_COLLECTION_LOCAL="local"

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
# Start of database specific backup
# **************************************

# Export MongoDB instances with wind upon request (weekly parameter provided)
for MONGO_PORT in $MONGO_PORTS; do
	echo Backing up MongoDB running on port $MONGO_PORT
	MONGODB_DATABASES=`echo "show dbs" | $MONGO_CMD --port $MONGO_PORT | tail -n +3 | grep -v "^bye$" | awk '{print $1;}'`
	for i in $MONGODB_DATABASES; do
		echo "  Backing up database $i in MongoDB running on port $MONGO_PORT"
		MONGODB_COLLECTIONS=`echo "use $i
show collections" | $MONGO_CMD --port $MONGO_PORT | tail -n +4 | grep -v "^system.indexes$" | grep -v "^bye$"`
		echo "Collections in MongoDB $i running on port $MONGO_PORT: $MONGODB_COLLECTIONS"
		for j in $MONGODB_COLLECTIONS; do
			if [ "$j" = "$MONGODB_COLLECTION_WIND" ]; then
				WIND_SUFFIX="-wind"
			else
				WIND_SUFFIX=
			fi
			if [ "$i" != "winddb" ]; then
				DB_DIR=$TARGET_DIR/mongodb-$MONGO_PORT-$i$WIND_SUFFIX
			else
				DB_DIR=$TARGET_DIR/mongodb-$MONGO_PORT$WIND_SUFFIX
			fi
			if [ "$i" != "$MONGODB_COLLECTION_LOCAL" -a \( "$i" != "$MONGODB_COLLECTION_WIND" -o "$PARAM" = "weekly" \) ]; then
				echo "    Backing up collection $j in MongoDB $i running on port $MONGO_PORT"
				echo "$MONGOEXPORT_CMD --port $MONGO_PORT -d $i -c $j > $DB_DIR/$j.json"
				mkdir -p $DB_DIR
				$MONGOEXPORT_CMD --port $MONGO_PORT -d $i -c $j > $DB_DIR/$j.json
			fi
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
