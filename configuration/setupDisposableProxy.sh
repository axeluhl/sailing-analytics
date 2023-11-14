#!/bin/bash
BEARER_TOKEN=$1
yum update -y
yum install -y perl httpd mod_proxy_html tmux nfs-utils git whois jq
sudo amazon-linux-extras install epel -y && yum install -y apachetop
sed -i 's/.*sleep 10" //g' ~/.ssh/authorized_keys
sed -i 's/#PermitRootLogin yes/PermitRootLogin without-password/' /etc/ssh/sshd_config
ln -s  /home/wiki/gitwiki/configuration/update_authorized_keys_for_landscape_managers /usr/local/bin/update_authorized_keys_for_landscape_managers
ln -s  /home/wiki/gitwiki/configuration/update_authorized_keys_for_landscape_managers_if_changed /usr/local/bin/update_authorized_keys_for_landscape_managers_if_changed
ln -s  /home/wiki/gitwiki/configuration/crontab /root/crontab
echo $BEARER_TOKEN > /root/ssh-key-reader.token
crontab /root/crontab
mv /etc/httpd/conf.d/welcome.conf /etc/httpd/conf.d/welcome_backup
cat <<EOF > /var/www/html/index.html
<!DOCTYPE html><html lang="en"><head><title>Health check</title><meta charset="UTF-8"></head><body><h1>Test page</h1></body></html>
EOF
systemctl enable httpd
echo "net.ipv4.ip_conntrac_max = 131072" > /etc/sysctl.conf   #should I have appended to limits.conf
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
yum install -y mod_ssl mod_perl mod_php


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