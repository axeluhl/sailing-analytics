#!/bin/bash

# Setup script for Amazon Linux 2. May need to update macro definitions for the archive IP. 
# Parameter 1 is the IP and parameter 2 is the bearer token to be installed in the root home dir.
IP=$1
BEARER_TOKEN=$2
HTTP_LOGROTATE_ABSOLUTE=/etc/logrotate.d/httpd
GIT_COPY_USER="trac"
RELATIVE_PATH_TO_GIT="gitcopy" # the relative path to the repo within the git_copy_user
HTTPD_GIT_REPO_IP="172.31.40.235" # points to where git repo is
AWS_CREDENTIALS_IP="34.251.204.62" # points to a server which has no-mfa credentials within the root user, possibly the central reverse proxy. 
ssh -A "ec2-user@${IP}" "bash -s" << FIRSTEOF 
# Correct authorized keys. May not be necessary if update_authorized_keys is running.
sudo su - -c "cat ~ec2-user/.ssh/authorized_keys > /root/.ssh/authorized_keys"
FIRSTEOF
# writes std error to local text file
ssh -A "root@${IP}" "bash -s" << SECONDEOF  >log.txt    
sed -i 's/#PermitRootLogin yes/PermitRootLogin without-password\nPermitRootLogin yes/' /etc/ssh/sshd_config
sed -i 's/^disable_root: true$/disable_root: false/' /etc/cloud/cloud.cfg
# fstab setup
mkdir /var/log/old
echo "logfiles.internal.sapsailing.com:/var/log/old   /var/log/old    nfs     tcp,intr,timeo=100,retry=0" >> /etc/fstab
mount -a
# update instance
yum update -y
yum install -y httpd mod_proxy_html tmux nfs-utils git whois jq mailx postfix cronie iptables
service postfix restart
sudo systemctl enable crond.service

# setup other users and crontabs to keep repo updated
cd /home
GIT_SSH_COMMAND="ssh -A -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"  git clone ssh://trac@sapsailing.com/home/trac/git
adduser ${GIT_COPY_USER}
    mv git ${GIT_COPY_USER}/${RELATIVE_PATH_TO_GIT}
scp -o StrictHostKeyChecking=no -r "root@sapsailing.com:/home/wiki/.ssh" "/home/${GIT_COPY_USER}"  # copies wiki users passwordless keys
chown -R "${GIT_COPY_USER}":"${GIT_COPY_USER}" "${GIT_COPY_USER}"
# setup symbolic links and crontab
cd "/home/${GIT_COPY_USER}/${RELATIVE_PATH_TO_GIT}/"
./build-crontab reverse_proxy "${GIT_COPY_USER}" "${RELATIVE_PATH_TO_GIT}"
cd /usr/local/bin
echo $BEARER_TOKEN > /root/ssh-key-reader.token
crontab /root/crontab
# add basic test page which won't cause redirect error code if used as a health check.
cat <<EOF > /var/www/html/index.html
<!DOCTYPE html><html lang="en"><head><title>Health check</title><meta charset="UTF-8"></head><body><h1>Test page</h1></body></html>
EOF
# ensure httpd starts on startup
systemctl enable httpd
echo "net.ipv4.ip_conntrac_max = 131072" >> /etc/sysctl.conf
# setup fail2ban
if [[ ! -f "/etc/systemd/system/fail2ban.service" ]]; then 
    yum install 2to3 -y
    wget https://github.com/fail2ban/fail2ban/archive/refs/tags/1.0.2.tar.gz
    tar -xvf 1.0.2.tar.gz
    cd fail2ban-1.0.2/
    ./fail2ban-2to3
    python3.9 setup.py build
    python3.9 setup.py install
    cp ./build/fail2ban.service /etc/systemd/system/fail2ban.service
    sed -i 's|Environment=".*"|Environment="PYTHONPATH=/usr/local/lib/python3.9/site-packages"|' /etc/systemd/system/fail2ban.service
    systemctl enable fail2ban
    systemctl start fail2ban
    chkconfig --level 23 fail2ban on
fi
cat <<EOF > /etc/fail2ban/jail.d/customisation.local
[ssh-iptables]

enabled  = true
filter   = sshd[mode=aggressive]
action   = iptables[name=SSH, port=ssh, protocol=tcp]
           sendmail-whois[name=SSH, dest=thomasstokes@yahoo.co.uk, sender=fail2ban@sapsailing.com]
logpath  = /var/log/fail2ban.log
maxretry = 5
EOF
service fail2ban start
yum remove -y firewalld
yum install -y mod_ssl
# setup mounting of nvme
source /root/.bashrc
mountnvmeswap
systemctl enable mountnvmeswap.service
systemctl enable register-deregister-nlb.service
systemctl enable get-latest-httpd-conf.service
# setup logrotate.d/httpd 
mkdir /var/log/logrotate-target
echo "Patching $HTTP_LOGROTATE_ABSOLUTE so that old logs go to /var/log/old/$IP" >>/var/log/sailing.out
mkdir --parents "/var/log/old/REVERSE_PROXIES/${IP}"
sed -i  "s|/var/log/old|/var/log/old/REVERSE_PROXIES/${IP}|" $HTTP_LOGROTATE_ABSOLUTE 
# logrotate.conf setup
sed -i 's/rotate 4/rotate 20 \n\nolddir \/var\/log\/logrotate-target/' /etc/logrotate.conf
sed -i "s/^#compress/compress/" /etc/logrotate.conf
# setup latest cli

if [[ ! -d "/root/aws" ]]; then 
    yum remove -y awscli
    cd ~ && curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
    cd ~ && unzip awscliv2.zip
    rm -rf awscliv2.zip
    cd ~ && sudo ./aws/install
fi
# setup git
/root/setupHttpdGitLocal.sh "httpdConf@${HTTPD_GIT_REPO_IP}:repo.git"
# copy httpd key accross
scp -o StrictHostKeyChecking=no "httpdConf@${HTTPD_GIT_REPO_IP}:~/.ssh/id_ed25519" /root/.ssh
scp -o StrictHostKeyChecking=no "httpdConf@${HTTPD_GIT_REPO_IP}:~/.ssh/id_ed25519.pub" /root/.ssh/temp
cat /root/.ssh/temp  >> /root/.ssh/authorized_keys
rm /root/.ssh/temp
# certs setup
cd /etc
mkdir letsencrypt
mkdir letsencrypt/live
mkdir letsencrypt/live/sail-insight.com
scp -o StrictHostKeyChecking=no -r root@sapsailing.com:/etc/letsencrypt/live/sail-insight.com/* /etc/letsencrypt/live/sail-insight.com
# copy aws credentials to apache user
scp -o StrictHostKeyChecking=no  -r "root@${AWS_CREDENTIALS_IP}:~/.aws"  /usr/share/httpd
sed -i "s/region = .*/region = \$(curl http://169.254.169.254/latest/meta-data/placement/region)/" /usr/share/httpd/.aws/config  #ensure the IMDSv2 metadata is optional
cp -r /usr/share/httpd/.aws /root
chown -R apache:apache /usr/share/httpd
# setup releases
# rsync -av --delete root@sapsailing.com:/var/www/static/releases /var/www/static/releases
# # setup p2
# rsync -av --delete trac@sapsailing.com:p2-repositories /home/trac

systemctl start httpd
sudo systemctl start crond.service
sudo systemctl enable postfix
SECONDEOF

