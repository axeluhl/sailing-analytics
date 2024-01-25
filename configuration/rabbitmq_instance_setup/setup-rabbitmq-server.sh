#!/bin/bash
# Apply this to an instance launched from a barebones Debian 12 image with ~16GB of root volume size.
# As a result, you'll get a ready-to-use RabbitMQ server that is running on the default port and accepts
# connections with the guest user log-in also from non-localhost addresses.
if [ $# != 0 ]; then
  SERVER=$1
  scp -o StrictHostKeyChecking=false "${0}" admin@${SERVER}:
  ssh -o StrictHostKeyChecking=false -A admin@${SERVER} ./$( basename "${0}" )
else
  # Fix the non-sensical use of "dash" as the default shell:
  sudo rm /usr/bin/sh
  sudo ln -s /usr/bin/bash /usr/bin/sh
  scp -o StrictHostKeyChecking=false root@sapsailing.com:ssh-key-reader.token /home/admin
  sudo chown admin /home/admin/ssh-key-reader.token
  sudo chgrp admin /home/admin/ssh-key-reader.token
  sudo chmod 600 /home/admin/ssh-key-reader.token
  # Install packages for MariaDB and cron/anacron/crontab:
  sudo apt-get -y update
  sudo DEBIAN_FRONTEND=noninteractive apt-get -yq -o Dpkg::Options::=--force-confdef -o Dpkg::Options::=--force-confnew upgrade
  sudo DEBIAN_FRONTEND=noninteractive apt-get -yq -o Dpkg::Options::=--force-confdef -o Dpkg::Options::=--force-confnew install rabbitmq-server systemd-cron jq syslog-ng
  sudo touch /var/run/last_change_aws_landscape_managers_ssh_keys
  sudo chown admin:admin /var/run/last_change_aws_landscape_managers_ssh_keys
  scp -o StrictHostKeyChecking=false -r root@sapsailing.com:/home/wiki/gitwiki/configuration/environments_scripts /home/admin
  sudo su -c "/home/admin/environments_scripts/build-crontab 'rabbitmq_instance_setup' admin environments_scripts"
  # Wait for RabbitMQ to become available; note that install under apt also means start...
  sleep 10
  sudo rabbitmq-plugins enable rabbitmq_management
  # Allow guest login from non-localhost IPs:
  sudo su - -c "cat <<EOF >>/etc/rabbitmq/rabbitmq.conf
loopback_users = none
EOF
"
  sudo systemctl restart rabbitmq-server.service
  echo 'Test your DB, e.g., by counting bugs: sudo mysql -u root -p -e "use bugs; select count(*) from bugs;"'
  echo "If you like what you see, switch to the new DB by updating the mysql.internal.sapsailing.com DNS record to this instance,"
  echo "make sure the instance has the \"Database and Messaging\" security group set,"
  echo "and tag the instance's root volume with the WeeklySailingInfrastructureBackup=Yes tag."
fi
