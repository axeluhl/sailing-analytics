#!/bin/bash

# Setup script for Amazon Linux 2. May need to update macro definitions for the archive IP. 
# Parameter 1 is the IP and parameter 2 is the bearer token to be installed in the root home dir. 
# Ensure that the security for requesting the metadata uses IMDSv1
IP=$1
BEARER_TOKEN=$2
IMAGE_TYPE="reverse_proxy"
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
# fstab setup
mkdir /var/log/old
echo "logfiles.internal.sapsailing.com:/var/log/old   /var/log/old    nfs     tcp,intr,timeo=100,retry=0" >> /etc/fstab
mount -a
# update instance
yum update -y
yum install -y httpd mod_proxy_html tmux nfs-utils git whois jq mailx postfix cronie iptables mod_ssl
service postfix restart
sudo systemctl enable postfix
sudo systemctl enable crond.service

# setup other users and crontabs to keep repo updated
cd /home
if [[ ! -d "/home/trac" ]]; then
    scp -o StrictHostKeyChecking=no -r "root@sapsailing.com:/home/wiki/gitwiki/configuration/environments_scripts" "/home/git"
    adduser ${GIT_COPY_USER}
    mv git ${GIT_COPY_USER}/${RELATIVE_PATH_TO_GIT}
    mkdir /home/${GIT_COPY_USER}/.ssh
    cd "/home/${GIT_COPY_USER}/${RELATIVE_PATH_TO_GIT}/"
    ./build-crontab-and-cp-files -n reverse_proxy  "${GIT_COPY_USER}" "${RELATIVE_PATH_TO_GIT}" # -n option doesn't install the crontab so the correct keys get added before update_authorized_keys runs.
    # Setup root user and trac user with the right keys
    . imageupgrade_functions.sh
    setup_keys "${IMAGE_TYPE}"
    # copy httpd key accross
    scp -o StrictHostKeyChecking=no "httpdConf@${HTTPD_GIT_REPO_IP}:~/.ssh/id_ed25519.pub" /root/.ssh/temp
    cat /root/.ssh/temp  >> /root/.ssh/authorized_keys
    rm /root/.ssh/temp
    chown -R "${GIT_COPY_USER}":"${GIT_COPY_USER}" "/home/${GIT_COPY_USER}"
fi
# setup symbolic links and crontab
cd "/home/${GIT_COPY_USER}/${RELATIVE_PATH_TO_GIT}/"
./build-crontab-and-cp-files -s reverse_proxy  "${GIT_COPY_USER}" "${RELATIVE_PATH_TO_GIT}"
cd /usr/local/bin
echo $BEARER_TOKEN > /root/ssh-key-reader.token
# add basic test page which won't cause redirect error code if used as a health check.
cat <<EOF > /var/www/html/index.html
<!DOCTYPE html><html lang="en"><head><title>Health check</title><meta charset="UTF-8"></head><body><h1>Test page</h1></body></html>
EOF
# ensure httpd starts on startup
systemctl enable httpd
echo "net.ipv4.ip_conntrac_max = 131072" >> /etc/sysctl.conf
# setup fail2ban
. imageupgrade_functions.sh
setup_fail2ban
# setup mounting of nvme
mountnvmeswap
# setup logrotate.d/httpd 
mkdir /var/log/logrotate-target
echo "Patching $HTTP_LOGROTATE_ABSOLUTE so that old logs go to /var/log/old/$IP" >>/var/log/sailing.out
mkdir --parents "/var/log/old/REVERSE_PROXIES/${IP}"
sed -i  "s|/var/log/old|/var/log/old/REVERSE_PROXIES/${IP}|" $HTTP_LOGROTATE_ABSOLUTE 
# logrotate.conf setup
sed -i 's/rotate 4/rotate 20 \n\nolddir \/var\/log\/logrotate-target/' /etc/logrotate.conf
sed -i "s/^#compress/compress/" /etc/logrotate.conf
# setup git
/root/setupHttpdGitLocal.sh "httpdConf@${HTTPD_GIT_REPO_IP}:repo.git"
cd /etc
# copy aws credentials to apache user
scp -o StrictHostKeyChecking=no  -r "root@${AWS_CREDENTIALS_IP}:~/.aws"  /usr/share/httpd
sed -i "s/region = .*/region = \$(curl http://169.254.169.254/latest/meta-data/placement/region)/" /usr/share/httpd/.aws/config  #ensure the IMDSv2 metadata is optional
cp -rp /usr/share/httpd/.aws /root
chown -R apache:apache /usr/share/httpd
# Final enabling and starting of services.
systemctl start httpd
sudo systemctl start crond.service
systemctl enable imageupgrade.service
SECONDEOF