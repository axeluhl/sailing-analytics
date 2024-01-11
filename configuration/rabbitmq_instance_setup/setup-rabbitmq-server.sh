#!/bin/bash
# Apply this to an instance launched from a barebones Debian 12 image with ~16GB of root volume size.
# As a result, you'll get a ready-to-use RabbitMQ server that is running on the default port and accepts
# connections with the guest user log-in also from non-localhost addresses.
if [ $# != 0 ]; then
  SERVER=$1
  scp -o StrictHostKeyChecking=false "${0}" admin@${SERVER}:
  ssh -o StrictHostKeyChecking=false -A admin@${SERVER} ./$( basename "${0}" )
else
  # Install cron job for ssh key update for landscape managers
  scp -o StrictHostKeyChecking=false root@sapsailing.com:/home/wiki/gitwiki/configuration/update_authorized_keys_for_landscape_managers /tmp
  sudo mv /tmp/update_authorized_keys_for_landscape_managers /usr/local/bin
  scp -o StrictHostKeyChecking=false root@sapsailing.com:/home/wiki/gitwiki/configuration/update_authorized_keys_for_landscape_managers_if_changed /tmp
  sudo mv /tmp/update_authorized_keys_for_landscape_managers_if_changed /usr/local/bin
  scp -o StrictHostKeyChecking=false root@sapsailing.com:/home/wiki/gitwiki/configuration/rabbitmq_instance_setup/crontab-admin /home/admin/crontab
  scp -o StrictHostKeyChecking=false root@sapsailing.com:ssh-key-reader.token /home/admin
  sudo chown admin /home/admin/ssh-key-reader.token
  sudo chgrp admin /home/admin/ssh-key-reader.token
  sudo chmod 600 /home/admin/ssh-key-reader.token
  # Install packages for MariaDB and cron/anacron/crontab:
  sudo apt-get update
  sudo apt-get upgrade
  sudo apt-get install rabbitmq-server systemd-cron
  crontab /home/admin/crontab
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
