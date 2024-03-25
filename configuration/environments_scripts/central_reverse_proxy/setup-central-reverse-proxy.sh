#!/bin/bash

# Setup script for Amazon Linux 2. May need to update macro definitions for the archive IP. 
# Parameter 1 is the IP and parameter 2 is the bearer token to be installed in the root home dir.
IP=$1
BEARER_TOKEN=$2
IMAGE_TYPE="central_reverse_proxy"
HTTP_LOGROTATE_ABSOLUTE=/etc/logrotate.d/httpd
# The aws credentials will have to be manually installed in the aws user.
ssh -A "ec2-user@${IP}" "bash -s" << FIRSTEOF 
# Correct authorized keys. May not be necessary if update_authorized_keys is running.
sudo su - -c "cat ~ec2-user/.ssh/authorized_keys > /root/.ssh/authorized_keys"
FIRSTEOF
# writes std error to local text file
ssh -A "root@${IP}" "bash -s" << SECONDEOF  >log.txt    
# update instance
yum update -y
yum install -y httpd mod_proxy_html tmux nfs-utils git whois jq cronie iptables mailx nmap gcc-c++ geoip-devel icu libicu-devel docker mariadb-server perl-GD
yum install -y perl perl-CGI perl-Template-Toolkit  perl-CPAN perl-DBD-MySQL mod_perl
amazon-linux-extras install epel -y && yum install -y apachetop
# main conf mandates php7.1
amazon-linux-extras enable php7.1
yum install -y php # install mod_phpservice
# make root readable for 
chmod 755 /root
# setup cloud_cfg and keys
cd /home
scp -o StrictHostKeyChecking=no -p "root@sapsailing.com:/home/wiki/gitwiki/configuration/environments_scripts/repo/usr/local/bin/imageupgrade_functions.sh" /usr/local/bin
setup_keys "${IMAGE_TYPE}"
setup_cloud_cfg_and_root_login
# setup symbolic links and crontab
## build_crontab_and_setup_files "${IMAGE_TYPE}" "${GIT_COPY_USER}" "${RELATIVE_PATH_TO_GIT}"   # THIS MUST BE RUN AFTER MOUNTING
# setup mail
setup_mail_sending
# setup sshd config
setup_sshd_resilience
# copy bugzilla
scp root@sapsailing.com:/var/www/static/bugzilla-5.0.4.tar.gz /root
tar -xzvf bugzilla-5.0.4.tar.gz -C /usr/share/bugzilla
mv /usr/share/bugzilla-5.0.4 /usr/share/bugzilla
cd /usr/share/bugzilla/
scp root@sapsailing.com:/usr/share/bugzilla/localconfig .
# missing perl modules
# essentials bugzilla
/usr/bin/perl install-module.pl DateTime
/usr/bin/perl install-module.pl DateTime::TimeZone
/usr/bin/perl install-module.pl Email::Sender
/usr/bin/perl install-module.pl Email::MIME
/usr/bin/perl install-module.pl List::MoreUtils
/usr/bin/perl install-module.pl Math::Random::ISAAC
/usr/bin/perl install-module.pl JSON::XS

# important bugzilla
/usr/bin/perl install-module.pl Email::Address
/usr/bin/perl install-module.pl autodie
/usr/bin/perl install-module.pl Class::XSAccessor
# nice to have for buzilla
/usr/bin/perl install-module.pl Date::Parse
/usr/bin/perl install-module.pl Email::Send
/usr/bin/perl install-module.pl DBI
/usr/bin/perl install-module.pl Geo::IP::PurePerl
/usr/bin/perl install-module.pl IO::Socket::SSL
/usr/bin/perl install-module.pl Chart::Lines
/usr/bin/perl install-module.pl Template::Plugin::GD::Image
/usr/bin/perl install-module.pl GD::Text
/usr/bin/perl install-module.pl GD::Graph
/usr/bin/perl install-module.pl PatchReader
/usr/bin/perl install-module.pl Authen::Radius
/usr/bin/perl install-module.pl JSON::RPC
/usr/bin/perl install-module.pl TheSchwartz
/usr/bin/perl install-module.pl Daemon::Generic
/usr/bin/perl install-module.pl File::MimeInfo::Magic
/usr/bin/perl install-module.pl File::Copy::Recursive
# use the localconfig file to setup the bugzilla
./checksetup.pl
# append hostname to sysconfig
echo "HOSTNAME=sapsailing.com" >> /etc/sysconfig/network
sed -i "s/\(127.0.0.1 *\)/\1 sapsailing.com /" /etc/hosts
hostname sapsailing.com
hostnamectl set-hostname sapsailing.com
echo $BEARER_TOKEN > /root/ssh-key-reader.token
# add basic test page which won't cause redirect error code if used as a health check.
cat <<EOF > /var/www/html/index.html
<!DOCTYPE html><html lang="en"><head><title>Health check</title><meta charset="UTF-8"></head><body><h1>Test page</h1></body></html>
EOF

echo "net.ipv4.ip_conntrac_max = 131072" >> /etc/sysctl.conf
# setup fail2ban
yum install -y fail2ban
cat <<EOF > /etc/fail2ban/jail.d/customisation.local
[ssh-iptables]

enabled  = true
filter   = sshd[mode=aggressive]
action   = iptables[name=SSH, port=ssh, protocol=tcp]
           sendmail-whois[name=SSH, dest=thomasstokes@yahoo.co.uk, sender=fail2ban@sapsailing.com]
logpath  = /var/log/fail2ban.log
maxretry = 5
EOF
chkconfig --level 23 fail2ban on
service fail2ban start
yum remove -y firewalld
# setup logrotate.d/httpd 
mkdir /var/log/logrotate-target
echo "Patching $HTTP_LOGROTATE_ABSOLUTE so that old logs go to /var/log/old/$IP" >>/var/log/sailing.out
mkdir --parents "/var/log/old/REVERSE_PROXIES/${IP}"
sed -i  "s|/var/log/old|/var/log/old/REVERSE_PROXIES/${IP}|" $HTTP_LOGROTATE_ABSOLUTE 
# logrotate.conf setup
sed -i 's/rotate 4/rotate 20 \n\nolddir \/var\/log\/logrotate-target/' /etc/logrotate.conf
sed -i "s/^#compress/compress/" /etc/logrotate.conf
# setup httpd git
/root/setupHttpdGitLocal.sh "httpdConf@${HTTPD_GIT_REPO_IP}:repo.git"
# certs setup
cd /etc
mkdir letsencrypt
mkdir letsencrypt/live
mkdir letsencrypt/live/sail-insight.com
scp -o StrictHostKeyChecking=no -r root@sapsailing.com:/etc/letsencrypt/live/sail-insight.com/* /etc/letsencrypt/live/sail-insight.com
# enable units which build-crontab doesn't 
systemctl enable httpd
systemctl start httpd
sudo systemctl start crond.service
sudo systemctl enable crond.service
chkconfig sendmail off
sudo systemctl enable postfix
sudo systemctl restart postfix

# tmux setup?
# mongo
# git passwds?
# anything in etc
SECONDEOF



#not available: perl-HTML-Template  /usr/bin/perl install-module.pl GD
