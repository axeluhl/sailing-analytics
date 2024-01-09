#!/bin/bash
# Usage: ${0} [ -b {bugs-password] ] [ -r {root-password} ] {instance-ip}

BACKUP_FILE=/tmp/backupdb.sql
options='b:r:'
while getopts $options option
do
    case $option in
        b) BUGS_PW="${OPTARG}" ;;
        r) ROOT_PW="${OPTARG}" ;;
        \?) echo "Invalid option"
            exit 4;;
    esac
done
if [ -z "${ROOT_PW}" ]; then
  echo -n "MySQL password for user root: "
  read -s ROOT_PW
fi
if [ -z "${BUGS_PW}" ]; then
  echo -n "MySQL password for user bugs: "
  read -s BUGS_PW
fi
sudo yum update -y
sudo yum install mariadb105-server
sudo systemctl enable mariadb.service
sudo systemctl start mariadb.service
cat <<'EOF' >${BACKUP_FILE}
-- The following two lines added manually, based on
-- https://dba.stackexchange.com/questions/266480/mariadb-mysql-all-db-import-table-user-already-exists
DROP TABLE IF EXISTS `mysql`.`global_priv`;
DROP VIEW IF EXISTS `mysql`.`user`;
EOF
echo "Creating backup through mysql client on sapsailing.com..."
ssh root@sapsailing.com "mysqldump --all-databases -h mysql.internal.sapsailing.com --user=root --password=${ROOT_PW} --master-data" >> ${BACKUP_FILE}
echo "Importing backup locally..."
sudo mysql -u root -h localhost <${BACKUP_FILE}
sudo mysql -u root -h localhost -e "FLUSH PRIVILEGES;"
sudo systemctl stop mariadb.service
echo "Launching mysqld_safe to update user passwords..."
sudo mysqld_safe --skip-grant-tables --skip-networking &
while ! sudo mysql -u root -e "show databases;"; do
  echo "Waiting for mysqld_safe to become available..."
  sleep 5
done
mysql -u root -e "UPDATE mysql.user SET password=PASSWORD('${ROOT_PW}') WHERE user='root';"
mysql -u root -e "UPDATE mysql.user SET password=PASSWORD('${BUGS_PW}') WHERE user='bugs';"
mysql -u root -e "FLUSH PRIVILEGES;"
sudo mysqladmin -u root --password=${ROOT_PW} shutdown
sudo systemctl start mariadb.service
