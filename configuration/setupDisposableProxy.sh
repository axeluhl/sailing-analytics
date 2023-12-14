#!/bin/bash
BEARER_TOKEN=$1
INSTANCE_IP4=`ec2-metadata -v | cut -f2 -d " "`
HTTP_LOGROTATE=/etc/logrotate.d/httpd
#fstab
mkdir /var/log/old
echo "logfiles.internal.sapsailing.com:/var/log/old   /var/log/old    nfs     tcp,intr,timeo=100,retry=0" >> /etc/fstab
mount -a
#update
yum update -y
yum install -y httpd mod_proxy_html tmux nfs-utils git whois jq mailx
amazon-linux-extras install epel -y && yum install -y apachetop
# main conf mandates php7.1
amazon-linux-extras enable php7.1
yum install -y php  # also install mod_php
# Correct authorized keys. May not be necessary if update_authorized_keys is running.
sed -i 's/.*sleep 10" //g' ~/.ssh/authorized_keys
sed -i 's/#PermitRootLogin yes/PermitRootLogin without-password\nExitOnForwardFailure yes/' /etc/ssh/sshd_config
# setup other users and crontabs to keep repo updated
cd /home
GIT_SSH_COMMAND="ssh -A -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"  git clone ssh://trac@sapsailing.com/home/trac/git
adduser wiki
chown -R wiki:wiki wiki
crontab -u wiki /home/wiki/configuration/crontabs/cron-wiki
# setup symbolic links
cd /usr/local/bin
ln -s  /home/wiki/configuration/update_authorized_keys_for_landscape_managers /usr/local/bin/update_authorized_keys_for_landscape_managers
ln -s  /home/wiki/configuration/update_authorized_keys_for_landscape_managers_if_changed /usr/local/bin/update_authorized_keys_for_landscape_managers_if_changed
ln -s  /home/wiki/configuration/on-site-scripts/paris2024/notify-operators
ln -s  /home/wiki/configuration/sync-repo-and-execute-cmd.sh
ln -s  /home/wiki/configuration/switchoverArchive.sh 

ln -s  /home/wiki/configuration/crontab /root/crontab   # make sure to check the correct crontab is used
echo $BEARER_TOKEN > /root/ssh-key-reader.token
crontab /root/crontab
# add basic test page which won't cause redirect error code if used as a health check.
cat <<EOF > /var/www/html/index.html
<!DOCTYPE html><html lang="en"><head><title>Health check</title><meta charset="UTF-8"></head><body><h1>Test page</h1></body></html>
EOF
# ensure httpd starts on startup
systemctl enable httpd
echo "net.ipv4.ip_conntrac_max = 131072" >> /etc/sysctl.conf   # should I have appended to limits.conf too
# setup fail2ban
yum install -y fail2ban
cat <<EOF > /etc/fail2ban/jail.d/customisation.local
[ssh-iptables]

enabled  = true
filter   = sshd
action   = iptables[name=SSH, port=ssh, protocol=tcp]
           sendmail-whois[name=SSH, dest=thomasstokes@yahoo.co.uk, sender=fail2ban@sapsailing.com]
logpath  = /var/log/secure
maxretry = 5
EOF
chkconfig --level 23 fail2ban on
service fail2ban start
yum install -y mod_ssl
#setup mounting of nvme
cat <<EOF > /etc/systemd/system/mountnvmeswap.service
[Unit]
Description=An unformatted nvme is turned into swap space
Requires=-.mount
After=-.mount

[Install]

[Service]
Type=oneshot
RemainAfterExit=true
ExecStart=/usr/local/bin/mountnvmeswap
EOF
## ln -s /home/wiki/git/configuration/archive_instance_setup/mountnvmeswap /usr/local/bin/mountnvmeswap
ln -s /root/mountnvmeswap /usr/local/bin/mountnvmeswap
source /root/.bashrc
./mountnvmeswap
#logrotate.d/httpd
echo "Patching $HTTP_LOGROTATE so that old logs go to /var/log/old/$INSTANCE_IP4" >>/var/log/sailing.out
sed -i -e "s/\/var\/log\/old\/\([^/]*\)\/\([^/ ]*\)/\/var\/log\/old\/$INSTANCE_IP4/" $HTTP_LOGROTATE
#logrotate.conf
sed -i 's/rotate 4/rotate 20 \n\nolddir \/var\/log\/logrotate-target/' /etc/logrotate.conf
# setup latest cli
yum remove -y awscli
cd ~ && curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
cd ~ && unzip awscliv2.zip
rm -rf awscliv2.zip
cd ~ && sudo ./aws/install

#setup git
## /home/wiki/git/configuration/setupHttpdGitLocal.sh "httpdConf@18.135.5.168:repo.git"
/root/setupHttpdGitLocal.sh "httpdConf@18.135.5.168:repo.git"

#certs
cd /etc
mkdir letsencrypt
mkdir letsencrypt/live
mkdir letsencrypt/live/sail-insight.com
scp -o StrictHostKeyChecking=no -r root@sapsailing.com:/etc/letsencrypt/live/sail-insight.com/* /etc/letsencrypt/live/sail-insight.com
systemctl start httpd


# will aws credentials need to be set? or will the ami store these details? session tokens? we will need a user without mfa.
# manual setup is mounts and tagging

