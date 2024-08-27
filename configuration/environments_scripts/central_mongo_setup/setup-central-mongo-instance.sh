#!/bin/bash
# Usage: Launch an Amazon EC2 instance from an Amazon Linux 2 AMI with
# 16GB of root partition size and the "Database and Messaging" security group
# using an SSH key for which you have a working private key available.
# Make sure to launch it in the same AZ as your current "MongoDB Central"
# instance, or you won't be able to attach the existing data volumes!
# Then, run this script on your local computer, using the external IP address
# of the instance you just launched in AWS as only argument. This will then
# prepare the instance as a central MongoDB instance.
# When the script is done you may log in to look around and check
# things. When done, stop the running "MongoDB Central" instance and detach
# its MongoDB data volumes as instructed below; attach them to the new instance,
# then run "mount -a" and start the MongoDB processes using the service units:
# - mongod-archive.service
# - mongod-hidden-live-replica.service
# - mongod-slow.service
# as per the instructions printed to the console at the end of this script.
# Name your instance "MongoDB Central" and assign the Route53 entry for
# dbserver.internal.sapsailing.com to its internal IP address.
if [ $# != 0 ]; then
  SERVER=$1
  scp "${0}" ec2-user@${SERVER}:
  ssh -A ec2-user@${SERVER} ./$( basename "${0}" )
else
  if ec2-metadata | grep -q instance-id; then
    echo "Running on an AWS EC2 instance as user ${USER} / $(whoami), starting setup..."
    # Install standard packages:
    sudo yum -y update
    sudo yum -y install nvme-cli chrony cronie cronie-anacron jq mailx
    # Copy imageupgrade_function.sh
    scp -o StrictHostKeyChecking=no -p root@sapsailing.com:/home/wiki/gitwiki/configuration/environments_scripts/repo/usr/local/bin/imageupgrade_functions.sh .
    sudo mv imageupgrade_functions.sh /usr/local/bin
    # build-crontab
    . imageupgrade_functions.sh
    build_crontab_and_setup_files central_mongo_setup
    # obtain root SSH key from key vault:
    setup_keys "central_mongo_setup"
    # Create some swap space for the case mountnvmeswap hasn't created any
    setup_swap 6000
    # Install MongoDB 4.4 and configure as replica set "live"
    setup_mongo_5_0
    # Disable default mongod service unit derived from /etc/init.d/mongod:
    sudo systemctl disable mongod.service
    # Prepare for the MongoDB volume mounts:
    sudo mkdir -p /var/lib/mongodb/hidden-live-replica
    sudo mkdir -p /var/lib/mongodb/slow
    sudo mkdir -p /var/lib/mongodb/archive
    sudo chown -R mongod:mongod /var/lib/mongodb
    sudo su - -c "cat << EOF >>/etc/fstab
UUID=d59156c5-7833-425a-9143-3a83b01f32b9	/var/lib/mongodb/hidden-live-replica	xfs	defaults,relatime	1	1
UUID=5586bc6e-d399-42e8-a0a0-bf2d65f8c703	/var/lib/mongodb/archive	xfs	defaults,relatime	1	1
UUID=0c73fde1-4719-405a-ab0e-bffd0aaf8d84	/var/lib/mongodb/slow	xfs	defaults,relatime	1	1
EOF
"
    scp root@sapsailing.com:ssh-key-reader.token /tmp
    sudo mv /tmp/ssh-key-reader.token /root
    sudo chown root:root /root/ssh-key-reader.token
    sudo chmod 600 /root/ssh-key-reader.token
    setup_sshd_resilience
    echo "Part 1 has completed. Now carry out the following steps in the AWS Console:"
    echo " - stop the \"MongoDB Central\" instance"
    echo " - detach the following volumes from it:"
    echo "   * Hidden MongoDB Live Replica encrypted"
    echo "   * MongoDB Archive winddb encrypted"
    echo "   * MongoDB Archive slow encrypted"
    echo " - attach these volumes to the new instance"
    echo " - issue the following commands on the new instance:"
    echo "   mount -a"
    echo "   chmod -R mongod:mongod /var/lib/mongodb"
    echo "   systemctl start mongod-archive.service"
    echo "   systemctl start mongod-hidden-live-replica.service"
    echo "   systemctl start mongod-slow.service"
    echo " - assign the Route53 DNS record \"dbserver.internal.sapsailing.com\" to this"
    echo "   instance's internal IP address $( ec2-metadata --local-ipv4 | sed -e 's/^local-ipv4: //' )"
    echo " - name the new instance \"MongoDB Central\" and terminate the old instance"
    echo " - tag the instance with mongo-replica-sets = live:10203,archive:10201,slow:10202"
  else
    echo "Not running on an AWS instance; refusing to run setup!" >&2
    echo "To prepare an instance running in AWS, provide its external IP as argument to this script." >&2
    exit 2
  fi
fi
