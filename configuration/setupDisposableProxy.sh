#!/bin/bash
BEARER_TOKEN=$1
MFA_NAME=$2
yum update -y
yum install -y httpd mod_proxy_html tmux nfs-utils git whois jq mailx
sudo amazon-linux-extras install epel -y && yum install -y apachetop
sudo -i
# main conf mandates php7.1
amazon-linux-extras enable php7.1
yum install -y php  # also install mod_php
# Correct authorized keys. May not be necessary if update_authorized_keys is running.
sed -i 's/.*sleep 10" //g' ~/.ssh/authorized_keys
sed -i 's/#PermitRootLogin yes/PermitRootLogin without-password\nExitOnForwardFailure yes/' /etc/ssh/sshd_config
# setup symbolic links
cd /usr/local/bin
ln -s  /home/wiki/gitwiki/configuration/update_authorized_keys_for_landscape_managers /usr/local/bin/update_authorized_keys_for_landscape_managers
ln -s  /home/wiki/gitwiki/configuration/update_authorized_keys_for_landscape_managers_if_changed /usr/local/bin/update_authorized_keys_for_landscape_managers_if_changed
ln -s  /home/wiki/gitwiki/configuration/crontab /root/crontab   # make sure to check the correct crontab is used
ln -s  /home/wiki/gitwiki/configuration/on-site-scripts/paris2024/notify-operators
ln -s  /home/wiki/gitwiki/configuration/sync-repo-and-execute-cmd.sh
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
Description=An unformatted /dev/nvme0n1 is turned into swap space
Requires=-.mount
After=-.mount

[Install]

[Service]
Type=oneshot
RemainAfterExit=true
ExecStart=/usr/local/bin/mountnvmeswap
EOF
ln -s /home/wiki/gitwiki/configuration/archive_instance_setup/mountnvmeswap /usr/local/bin/mountnvmeswap
sed -i "s/nvme0n1/nvme4n1" /usr/local/bin/mountnvmeswap
# setup mfa script
ln -s /home/wiki/gitwiki/configuration/aws-automation/awsmfalogon.sh /usr/local/bin
echo "alias awsmfa='echo -n "Token: "; read aws_mfa_token; . awsmfalogon.sh "${MFA_NAME}" ${aws_mfa_token}' " >> root/.bashrc
source /root/.bashrc
#logrotate
sed -i 's/rotate 4/rotate 20 \n\nolddir \/var\/log\/logrotate-target/' /etc/logrotate.conf
# setup latest cli
yum remove awscli
cd ~ && curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
cd ~ && unzip awscliv2.zip
rm -rf awscliv2.zip
cd ~ && sudo ./aws/install
# setup other users and crontabs to keep repo updated
cd /home
chown -R trac:trac trac
adduser trac
su --login trac
crontab crontab
exit
chown -R wiki:wiki wiki
adduser wiki
su --login wiki
crontab crontab
exit
#setup git
/home/wiki/gitwiki/configuration/setupHttpdGitLocal.sh "httpdConf@18.135.5.168:repo.git"
# ensure welcome.conf doesn't conflict
# mv /etc/httpd/conf.d/welcome.conf /etc/httpd/conf.d/welcome.conf.bak   #may already be handled by git repo

# will aws credentials need to be set? or will the ami store these details? session tokens? we will need a user without mfa.
# manual setup is mounts and tagging

