#!/bin/bash
# Usage: ${0} [ -b {bugs-password] ] [ -r {root-password} ] {instance-ip}
# Deploy with Amazon Linux 2023

# Read options and assign to variables:
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
shift $((OPTIND-1))
if [ $# != 0 ]; then
  SERVER=$1
  scp -o StrictHostKeyChecking=false "${0}" ec2-user@${SERVER}:
  ssh -o StrictHostKeyChecking=false -A ec2-user@${SERVER} "./$( basename "${0}" ) -r \"${ROOT_PW}\" -b \"${BUGS_PW}\""
else
  BACKUP_FILE=/tmp/backupdb.sql
  backupdbNOLOCK=/tmp/backupdbNOLOCK.sql
  # Install cron job for ssh key update for landscape managers
  scp -o StrictHostKeyChecking=false root@sapsailing.com:/home/wiki/gitwiki/configuration/update_authorized_keys_for_landscape_managers /tmp
  sudo mv /tmp/update_authorized_keys_for_landscape_managers /usr/local/bin
  scp -o StrictHostKeyChecking=false root@sapsailing.com:/home/wiki/gitwiki/configuration/update_authorized_keys_for_landscape_managers_if_changed /tmp
  sudo mv /tmp/update_authorized_keys_for_landscape_managers_if_changed /usr/local/bin
  scp -o StrictHostKeyChecking=false root@sapsailing.com:/home/wiki/gitwiki/configuration/mysql_instance_setup/crontab-ec2-user /home/ec2-user/crontab
  scp -o StrictHostKeyChecking=false root@sapsailing.com:ssh-key-reader.token /home/ec2-user
  sudo chown ec2-user /home/ec2-user/ssh-key-reader.token
  sudo chgrp ec2-user /home/ec2-user/ssh-key-reader.token
  sudo chmod 600 /home/ec2-user/ssh-key-reader.token
  # Install packages for MariaDB and cron/anacron/crontab:
  sudo yum update -y
  sudo yum -y install mariadb105-server cronie
  sudo su -c "printf '[mysqld]\nlog_bin = /var/log/mariadb/mysql-bin.log' >> /etc/my.cnf.d/mariadb-server.cnf"
  sudo systemctl enable mariadb.service
  sudo systemctl start mariadb.service
  sudo systemctl enable crond.service
  sudo systemctl start crond.service
  crontab /home/ec2-user/crontab
  echo "Creating backup through mysql client on sapsailing.com..."
  ssh -o StrictHostKeyChecking=false root@sapsailing.com "mysqldump --all-databases -h mysql.internal.sapsailing.com --user=root --password=${ROOT_PW} --master-data  --ignore-table=mysql.user --skip-lock-tables  --lock-tables=0 " >> ${BACKUP_FILE}
  echo "Removing lock on log table which causes failures"
  cat ${BACKUP_FILE} | sed  "/LOCK TABLES \`transaction_registry\`/,/UNLOCK TABLES;/d" >${backupdbNOLOCK}
  echo "Importing backup locally..."
  sudo mysql -u root -h localhost <${backupdbNOLOCK}
  echo "Creating new users, granting permissions for bugs and root, renaming root "
  sudo mysql -u root -e "use mysql; INSERT INTO \`tables_priv\` (\`Host\`, \`Db\`, \`User\`, \`Table_name\`, \`Grantor\`, \`Timestamp\`, \`Table_priv\`, \`Column_priv\`) VALUES ('localhost','mysql','mariadb.sys','global_priv','root@localhost','0000-00-00 00:00:00','Select,Delete','');"
  sudo mysql -u root -e "drop user bugs;create user bugs@'%' identified by  '${BUGS_PW}';"
  sudo mysql -u root -e "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES, EXECUTE, CREATE VIEW, SHOW VIEW, CREATE ROUTINE, ALTER ROUTINE ON \`bugs\`.* TO \`bugs\`@\`%\`"
  sudo mysql -u root -e "RENAME USER 'root'@'localhost' TO 'root'@'%' "
  sudo mysql -u root -e "alter user root@'%' identified by '${ROOT_PW}';"
  sudo mysql -u root -p${ROOT_PW} -e "FLUSH PRIVILEGES;"
  sudo systemctl stop mariadb.service
  sudo systemctl start mariadb.service
  sudo mysql -u root -p${ROOT_PW} -e "select count(bug_id) from bugs.bugs;"
  echo 'Test your DB, e.g., by counting bugs: sudo mysql -u root -p -e "use bugs; select count(*) from bugs;"'
  echo "If you like what you see, switch to the new DB by updating the mysql.internal.sapsailing.com DNS record to this instance,"
  echo "make sure the instance has the \"Database and Messaging\" security group set,"
  echo "and tag the instance's root volume with the WeeklySailingInfrastructureBackup=Yes tag."
fi
