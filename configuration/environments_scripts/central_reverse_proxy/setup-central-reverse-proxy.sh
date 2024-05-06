#!/bin/bash

# Setup script for a "Central Reverse HTTP Proxy" that runs Apache httpd,
# provides the Git repository, runs Bugzilla and Gollum for wiki access,
# furthermore AWStats, goaccess and apachetop support within a tmux session,
# releases.sapsailing.com, jobs.sapsailing.com content, and a Docker
# infrastructure for a self-hosted Docker image registry.
#
# Start by launching a new instance, e.g., of type m3.xlarge, in the same AZ
# as the current Webserver / Central Reverse Proxy. This will become important
# as you will need to detach volumes from the latter to attach them to the
# new instance.
#
# Then, call this script with the new instance's external IP address as the first,
# and with a "bearer token" authenticating a user at security-service.sapsailing.com
# which needs to have the following permissions:
#    USER:READ:*
#    SSH_KEY_READ:*
# which will be used to determine the landscape management users, for example.
#
# Example usage:
#   setup-central-reverse-proxy.sh 1.2.3.4 0OcJ1938QE5it875kjlQe7HnzQ6740jsnMEVzowjZrs=
# This will do all necessary set-up up to the point where the large volumes
# currently attached to and mounted on the current Central Reverse Proxy will
# need to be unmounted, detached, attached to the new instance, and mounted there.

IP=$1
BEARER_TOKEN=$2
IMAGE_TYPE="central_reverse_proxy"
HTTP_LOGROTATE_ABSOLUTE=/etc/logrotate.d/httpd
GIT_COPY_USER="wiki"
RELATIVE_PATH_TO_GIT="gitwiki" # the relative path to the repo within the git_copy_user
# The aws credentials will have to be manually installed in the aws user.
ssh -A "ec2-user@${IP}" "bash -s" << FIRSTEOF 
# Correct authorized keys. May not be necessary if update_authorized_keys is running.
sudo su - -c "cat ~ec2-user/.ssh/authorized_keys > /root/.ssh/authorized_keys"
FIRSTEOF
# writes std error to local text file
ssh -A "root@${IP}" "bash -s" << SECONDEOF  >log.txt    
# update instance
yum update -y
yum install -y httpd mod_proxy_html tmux nfs-utils git whois jq cronie iptables mailx nmap icu mariadb105-server tree #icu is a c/c++ library that provides unicode and globalisation support for software development.
# docker setup 
yum install -y docker
sudo curl -L "https://github.com/docker/compose/releases/download/v2.26.1/docker-compose-\$(uname -s)-\$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
yum install -y perl perl-CGI perl-Template-Toolkit  perl-CPAN perl-DBD-MySQL mod_perl perl-GD gcc-c++
# ruby and gollum for wiki
yum group install -y "Development Tools"
yum install -y ruby ruby-devel libicu libicu-devel zlib zlib-devel git cmake openssl-devel libyaml-devel
gem install gollum
gem update --system 3.5.7
cd /home
# scp -o StrictHostKeyChecking=no -p "root@sapsailing.com:/home/wiki/gitwiki/configuration/environments_scripts/repo/usr/local/bin/imageupgrade_functions.sh" /usr/local/bin
scp -o StrictHostKeyChecking=no -p "root@13.40.100.54:/home/sailing/code/configuration/environments_scripts/repo/usr/local/bin/imageupgrade_functions.sh" /usr/local/bin
. imageupgrade_functions.sh
setup_cloud_cfg_and_root_login
# setup files
build_crontab_and_setup_files -c -n "${IMAGE_TYPE}" "${GIT_COPY_USER}" "${RELATIVE_PATH_TO_GIT}"   # -c & -n mean only files are copied over.
cd /home
for folder in * ; do
    [[ -d "\$folder" ]] || continue
    grep "\$folder" /etc/passwd || continue
    chown -R "\$folder":"\$folder" "\$folder"
done
# setup mail
setup_mail_sending
# setup sshd config
setup_sshd_resilience
# setup goaccess and apachetop
setup_apachetop
setup_goaccess
# copy bugzilla
scp -o StrictHostKeyChecking=no  root@sapsailing.com:/var/www/static/bugzilla-5.0.4.tar.gz /usr/local/src
cd /usr/local/src
tar -xzvf bugzilla-5.0.4.tar.gz
mv bugzilla-5.0.4 /usr/share/bugzilla
cd /usr/share/bugzilla/
scp -o StrictHostKeyChecking=no  root@sapsailing.com:/usr/share/bugzilla/localconfig .
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
SECONDEOF
read -n 1  -p "Check bugzilla localconfig file and then press a key to continue" key_pressed
# t forces tty allocation.
ssh root@"${IP}" -A -t 'cd /usr/share/bugzilla/;  ./checksetup.pl'
ssh -A "root@${IP}" "cpan install Geo::IP"
ssh -A "root@${IP}" "bash -s" << THIRDEOF  >>log.txt    
. imageupgrade_functions.sh
echo $BEARER_TOKEN > /root/ssh-key-reader.token
# awstats - depends on some of the previous perl modules.
scp -o StrictHostKeyChecking=no  -r root@sapsailing.com:/usr/share/GeoIP /usr/share/GeoIP
cd /usr/local/src
wget http://prdownloads.sourceforge.net/awstats/awstats-7.0.tar.gz
tar -zvxf awstats-7.0.tar.gz
mv awstats-7.0/ /usr/share/awstats
mkdir /var/lib/awstats
scp -o StrictHostKeyChecking=no  -r root@sapsailing.com:/etc/awstats /etc/awstats
chmod 755 /root
cd ~
# copy awstats crons as well as all other weekly, daily cronjobs.
rsync -a --exclude perl5 root@sapsailing.com:/root /
rm -rf /root/.goaccessrc
scp -o StrictHostKeyChecking=no -r root@sapsailing.com:/etc/letsencrypt /etc
## TODO: Do we need to use awstats_configure.pl. No but we do need to do some copying of the GeoIP database. And get the conf file in order.
# add basic test page which won't cause redirect error code if used as a health check.
cat <<EOF > /var/www/html/index.html
<!DOCTYPE html><html lang="en"><head><title>Health check</title><meta charset="UTF-8"></head><body><h1>Test page</h1></body></html>
EOF
echo "net.ipv4.ip_conntrac_max = 131072" >> /etc/sysctl.conf
# setup fail2ban
setup_fail2ban
# setup logrotate.d/httpd 
# echo "Patching $HTTP_LOGROTATE_ABSOLUTE so that old logs go to /var/log/old/$IP" >>/var/log/sailing.out
# mkdir --parents "/var/log/old/REVERSE_PROXIES/${IP}"
# sed -i  "s|/var/log/old|/var/log/old/REVERSE_PROXIES/${IP}|" $HTTP_LOGROTATE_ABSOLUTE 
# logrotate.conf setup
sed -i 's/rotate 4/rotate 20 \n\nolddir \/var\/log\/logrotate-target/' /etc/logrotate.conf
sed -i "s/^#compress/compress/" /etc/logrotate.conf
# setup httpd git
(/usr/local/bin/setupHttpdGitLocal.sh "httpdConf@sapsailing.com:repo.git" central "Central Reverse Proxy")
scp -o StrictHostKeyChecking=no -r root@sapsailing.com:/etc/httpd/conf/pass* /etc/httpd/conf/
chown root:root /etc/httpd/conf/pass*
# create mountpoints (see part 2 for ownership changes)
mkdir /var/log/old
mkdir /var/www/static
# enable units which build-crontab doesn't 
systemctl enable httpd
systemctl start httpd
sudo systemctl enable crond.service
sudo systemctl enable postfix
sudo systemctl restart postfix
mkdir --parents /root/temporary_home_copy/home
mv /home/* /root/temporary_home_copy/home
echo "UUID=f03cc464-c3c0-452a-87da-e0eadc4c497f	/var/log	ext4	defaults,noatime,commit=30	0	0
UUID=23d42c52-85ee-4f6d-bdfe-c62f69bb689f	/home	ext4	defaults,noatime,commit=30	0	0
UUID=0b15f5cb-fd3e-48e6-8195-be248cd7726d	/var/www/static	ext3	defaults,noatime,commit=30	0	0
UUID=ff598428-d380-4429-a690-3809157506b7	/var/log/old	ext3	defaults,noatime,commit=30	0	0
UUID=d371e530-c189-4012-ae57-45d67a690554	/var/log/old/cache	ext4	defaults,noatime,commit=30	0	0" >>/etc/fstab
THIRDEOF

echo "Your turn! READ CAREFULLY! The instance is now prepared."
echo "Please ensure the existing central reverse proxy has been removed from the necessary target groups (draining can take 5 mins)"
echo "And that there is at least 1 healthy disposable in the SAME availability zone as the archive, so there is no risk of all the targets being briefly unhealthy."
echo "Then unmount the volumes /var/log, /home, /var/www/static, /var/log/old and /var/log/old/cache from the existing reverse proxy and remount them on the new instance."
echo "This can be done in the admin console by going to the webserver and clicking on the volumes in question (found within the storage tab)."
echo "Then click Detach from within the Actions column."
echo "For further details, checkout this wiki page https://wiki.sapsailing.com/wiki/info/landscape/amazon-ec2#amazon-ec2-for-sap-sailing-analytics_landscape-overview_apache-httpd-the-central-reverse-proxy-webserver-and-disposable-reverse-proxies"
echo "Once you are confident that this is working, please press a key to trigger part 2, which"
echo "sets up the hostname, copies /etc/ssh and configures the users and crontabs."
read -n 1  -p "Press a key to continue" key_pressed
"$(dirname $0)"/setup-central-reverse-proxy-part-2.sh "$IP" "$IMAGE_TYPE"

# anything in etc
#not available: perl-HTML-Template  /usr/bin/perl install-module.pl GD
