#!/bin/bash
if [ $# != 0 ]; then
  SERVER=$1
  scp "${0}" ec2-user@${SERVER}:
  ssh -A ec2-user@${SERVER} ./$( basename "${0}" )
else
  if ec2-metadata | grep -q instance-id; then
    echo "Running on an AWS EC2 instance as user ${USER} / $(whoami), starting setup..."
    # TODO: install JVM
    # install mountnvmeswap stuff
    # install /etc/init.d/sailing start-up script
    # mount /home
    sudo adduser sailing
    ssh-keygen -t ed25519 -P '' -f /home/ec2-user/.ssh/id_ed25519
    cat /home/ec2-user/.ssh/id_ed25519.pub | ssh root@sapsailing.com "cat >>/home/trac/.ssh/authorized_keys"
    sudo mkdir /home/sailing/.ssh
    sudo mv /home/ec2-user/.ssh/id* /home/sailing/.ssh
    sudo chown -R sailing /home/sailing/.ssh
    sudo chgrp -R sailing /home/sailing/.ssh
    sudo chmod 700 /home/sailing/.ssh
    sudo yum -y update
    sudo yum -y install git tmux
    sudo su - sailing -c "ssh -o StrictHostKeyChecking=false trac@sapsailing.com ls" >/dev/null
    sudo su - sailing -c "git clone ssh://trac@sapsailing.com/home/trac/git code"
  else
    echo "Not running on an AWS instance; refusing to run setup!" >&2
    exit 2
  fi
fi
