#!/bin/bash

# Setup script for Amazon Linux 2. May need to update macro definitions for the archive IP. 
# Parameter 1 is the IP and parameter 2 is the bearer token to be installed in the root home dir.
IP=$1
BEARER_TOKEN=$2
HTTP_LOGROTATE_ABSOLUTE=/etc/logrotate.d/httpd
GIT_COPY_USER="trac"
HTTPD_GIT_REPO_IP="18.135.5.168"
AWS_CREDENTIALS_IP="52.17.217.83"
ssh -A "ec2-user@${IP}" "bash -s" << FIRSTEOF 
# Correct authorized keys. May not be necessary if update_authorized_keys is running.
sudo -E bash <<NESTEDEOF
sed -i 's/.*sleep 10. //g' ~/.ssh/authorized_keys
NESTEDEOF
FIRSTEOF
# writes std error to local text file
ssh -A "root@${IP}" "bash -s" << SECONDEOF  >log.txt    
sed -i 's/#PermitRootLogin yes/PermitRootLogin without-password\nPermitRootLogin yes/' /etc/ssh/sshd_config

# fstab setup
mkdir /var/log/old
echo "logfiles.internal.sapsailing.com:/var/log/old   /var/log/old    nfs     tcp,intr,timeo=100,retry=0" >> /etc/fstab
mount -a
# update instance
yum update -y
yum install -y httpd mod_proxy_html tmux nfs-utils git whois jq mailx
amazon-linux-extras install epel -y && yum install -y apachetop
# main conf mandates php7.1
amazon-linux-extras enable php7.1
yum install -y php  # also install mod_php

# setup other users and crontabs to keep repo updated
cd /home
GIT_SSH_COMMAND="ssh -A -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"  git clone ssh://trac@sapsailing.com/home/trac/git
adduser ${GIT_COPY_USER}
mv git ${GIT_COPY_USER}/gitwiki
scp -o StrictHostKeyChecking=no -r "root@sapsailing.com:/home/wiki/.ssh" "/home/${GIT_COPY_USER}"  # copies wiki users passwordless keys
## crontab -u ${GIT_COPY_USER} "/home/${GIT_COPY_USER}/gitwiki/configuration/crontabs/users/crontab-wiki-user"
ln -s "/home/${GIT_COPY_USER}/gitwiki/configuration/syncgit" "/home/${GIT_COPY_USER}"
chown -R "${GIT_COPY_USER}":"${GIT_COPY_USER}" "${GIT_COPY_USER}"
# setup symbolic links
cd /usr/local/bin
ln -s  "/home/${GIT_COPY_USER}/gitwiki/configuration/update_authorized_keys_for_landscape_managers" /usr/local/bin/update_authorized_keys_for_landscape_managers
ln -s  "/home/${GIT_COPY_USER}/gitwiki/configuration/update_authorized_keys_for_landscape_managers_if_changed" /usr/local/bin/update_authorized_keys_for_landscape_managers_if_changed
ln -s  "/home/${GIT_COPY_USER}/gitwiki/configuration/on-site-scripts/paris2024/notify-operators"
ln -s  "/home/${GIT_COPY_USER}/gitwiki/configuration/sync-repo-and-execute-cmd.sh" /root
ln -s  /home/${GIT_COPY_USER}/gitwiki/configuration/switchoverArchive.sh 
ln -s  /home/${GIT_COPY_USER}/gitwiki/configuration/crontabs/environments/crontab-reverse-proxy /root/crontab   # make sure to check the correct crontab is used
## cp  /home/${GIT_COPY_USER}/gitwiki/configuration/httpd/cgi-bin/reverseProxyHealthcheck.sh /var/www/cgi-bin
cp /root/reverseProxyHealthcheck.sh /var/www/cgi-bin
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
yum install -y fail2ban
cat <<EOF > /etc/fail2ban/jail.d/customisation.local
[ssh-iptables]

enabled  = true
filter   = sshd[mode=aggressive]
action   = iptables[name=SSH, port=ssh, protocol=tcp]
           sendmail-whois[name=SSH, dest=thomasstokes@yahoo.co.uk, sender=fail2ban@sapsailing.com]
logpath  = /var/log/secure
maxretry = 5
EOF
chkconfig --level 23 fail2ban on
service fail2ban start
yum remove -y firewalld
yum install -y mod_ssl
# setup mounting of nvme
ln -s "/home/${GIT_COPY_USER}/gitwiki/configuration/archive_instance_setup/mountnvmeswap.service"  /etc/systemd/system/mountnvmeswap.service
## ln -s "/home/${GIT_COPY_USER}/gitwiki/configuration/archive_instance_setup/mountnvmeswap" /usr/local/bin/mountnvmeswap
source /root/.bashrc
./mountnvmeswap
systemctl enable mountnvmeswap.service
# setup logrotate.d/httpd 
mkdir /var/log/logrotate-target
echo "Patching $HTTP_LOGROTATE_ABSOLUTE so that old logs go to /var/log/old/$IP" >>/var/log/sailing.out
rm $HTTP_LOGROTATE_ABSOLUTE
ln -s "/home/${GIT_COPY_USER}/gitwiki/configuration/logrotate-httpd /etc/logrotate.d/httpd"
mkdir --parents "/var/log/old/REVERSE_PROXIES/${IP}"
sed -i -e "s|\/var\/log\/old|\/var\/log\/old\/REVERSE_PROXIES\/${IP}|" $HTTP_LOGROTATE_ABSOLUTE 
# logrotate.conf setup
sed -i 's/rotate 4/rotate 20 \n\nolddir \/var\/log\/logrotate-target/' /etc/logrotate.conf
sed -i "s/^#compress/compress/" /etc/logrotate.conf
# setup latest cli
yum remove -y awscli
cd ~ && curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
cd ~ && unzip awscliv2.zip
rm -rf awscliv2.zip
cd ~ && sudo ./aws/install

# setup git
## "/home/${GIT_COPY_USER}/gitwiki/configuration/setupHttpdGitLocal.sh" "httpdConf@18.135.5.168:repo.git"
/root/setupHttpdGitLocal.sh "httpdConf@18.135.5.168:repo.git"
# copy key accross
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
chown -R apache:apache /usr/share/httpd
sed -i "s/region = .*/region = \$(curl http://169.254.169.254/latest/meta-data/placement/region)/" /usr/share/httpd/.aws/config
# setup releases
# rsync -av --delete root@sapsailing.com:/var/www/static/releases /var/www/static/releases
# # setup p2
# rsync -av --delete trac@sapsailing.com:p2-repositories /home/trac

systemctl start httpd

SECONDEOF

