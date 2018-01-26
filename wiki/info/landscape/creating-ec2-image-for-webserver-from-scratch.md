# Setting up an image for the www.sapsailing.com web server

This is an add-on to the regular EC2 image set-up described [here](https://wiki.sapsailing.com/wiki/info/landscape/creating-ec2-image-from-scratch), but leave out the following packages during installation because they are not needed on the webserver:

* libstdc++48.i686 (for Android builds)
* glibc.i686 (for Android builds)
* libzip.i686 (for Android builds)
* telnet
* chrony (ntp is used now instead)

Then carry out these steps:

* install additional packages: `yum install fail2ban git mod24_perl perl perl-CGI perl-Template-Toolkit perl-HTML-Template perl-CPAN perl-DBD-MySQL mod24_ssl php71 php71-mysqlnd mod24-ldap ruby24 ruby24-devel rubygems24 rubygems24-devel icu libicu-devel gcc-c++ ncurses-devel geoip-devel perl-autodie`
* activate NFS by calling `chkconfig nfs on`; ensure that `/var/log/old` and `/home/scores` are exposed in `/etc/exports` as follows:
```
/var/log/old 172.31.0.0/16(rw,nohide,no_root_squash)
/home/scores 172.31.0.0/16(rw,nohide,no_root_squash)
```
* launch the NFS service once using `service nfs start`
* run the following command in order to obtain this feature required by Bugzilla:
```
cpan install Date::Parse Email::Address Email::Send DBI Geo::IP::PurePerl Math::Random::ISAAC
```
The libraries end up under `/root/perl5/lib/perl5`. For use by AWStats, read access to this path is required for the Apache web server. In particular, ensure that `/root` has read permissions for all.
* run the following commands to install missing Perl modules:
```
/usr/bin/perl install-module.pl DateTime::TimeZone
/usr/bin/perl install-module.pl Email::Sender
/usr/bin/perl install-module.pl GD
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
```
Those modules were installed to `/root/perl5/lib/perl5` but for some reason any `SetEnv PERL5LIB` directive in the Apache configuration for the bugzilla `VirtualHost` section seemd to remain ignored. Therefore, after installing all modules required, I copied all contents of `/root/perl5/lib/perl` to `/usr/local/share/perl5` to make them found through the `@INC` variable.
* Ensure that `/root/perl5/lib/perl5` is part of the `PERL5LIB` variable setting in the AWStats virtual host configuration in `/etc/httpd/conf.d/awstats.conf` as follows:
```
        <IfModule mod_env.c>
            SetEnv PERL5LIB /usr/share/awstats/lib:/usr/share/awstats/plugins:/root/perl5/lib/perl5
        </IfModule>
```
* make sure `/etc/alternatives/ruby` and `/etc/alternatives/gem` point to `/usr/bin/[ruby|gem]2.4`
* run the following commands to install gollum and uninstall a too current rack version 2.0.3:
```
gem install gollum
gem uninstall rack
Select gem to uninstall:
 1. rack-1.6.8
 2. rack-2.0.3
 3. All versions
> 2

You have requested to uninstall the gem:
        rack-2.0.3

sinatra-2.0.0 depends on rack (~> 2.0)
If you remove this gem, these dependencies will not be met.
Continue with Uninstall? [yN]  y
Successfully uninstalled rack-2.0.3

```
* ensure there are users and groups for `wiki`, `scores`, `wordpress`, `trac` that match up with their /home directory owners / groups
* ensure the Wiki startup script `serve.sh` configured for port 4567 and `config.ru` as well as the entire Gollum installation under /home/wiki are present, as well as the `users.yml` file
* ensure there is a reasonable `/root/.goaccess` file
* Configure goaccess by adjusting `/etc/goaccess.conf` such that it contains the following lines:
```
...
time-format %H:%M:%S
...
date-format %d/%b/%Y
...
# NCSA Combined with virtual host name as prefix:
log-format %v %h %^[%d:%t %^] "%r" %s %b "%R" "%u"
```
Note that the `log-format` piece is slightly different from the regular NCSA Combined Log Format in so far as it adds `%v` at the beginning which is capturing the virtual host name that our Apache servers are configured to log as the first field in each line.
* ensure there is the `/etc/tmux.conf` file that maps your hotkeys (Ctrl-a vs. Ctrl-b, for example)
* rename the `welcome.conf` file of the Apache configuration because it harms directory index presentation:
```
cd /etc/httpd/conf.d
mv welcome.conf welcome.conf.org
```
* install bugzilla to `/usr/share/bugzilla` and `/var/lib/bugzilla`
* create `/etc/bugzilla/localconfig`
* set up crontab for user `wiki` as `*/10 * * * * /home/wiki/syncgit` and make sure the script is in place
* ensure that `https://git.sapsailing.com/git` delivers the git content, with password credentials defined in `/etc/httpd/conf/passwd.git`. Sasa Zivkov (sasa.zivkov@sap.com) has been our point of contact of the SAP Gerrit group helping us with replicating our Git repository to the SAP-internal git.wdf.sap.corp one.
* comment `lbmethod_heartbeat_module` in /etc/httpd/conf.modules.d/00-proxy.conf because we don't need this sort of load balancing across origin servers and it causes a warning message in error_log
* install awstats to `/usr/share/awstats`, establish `/etc/httpd/conf/passwd.awstats`, establish a configuration under `/etc/awstats`, establish AWStats data directory under `/var/lib/awstats` and create /etc/cron.weekly/awstats as follows:
```
#!/bin/bash
su -l -c '/usr/share/awstats/tools/awstats_updateall.pl now         -configdir="/etc/awstats"         -awstatsprog="/usr/share/awstats/wwwroot/cgi-bin/awstats.pl" >>/var/log/awstats-cron.out 2>>/var/log/awstats-cron.err'
#exec /usr/share/awstats/tools/awstats_updateall.pl now         -configdir="/etc/awstats"         -awstatsprog="/usr/share/awstats/wwwroot/cgi-bin/awstats.pl" >>/var/log/awstats-cron.out 2>>/var/log/awstats-cron.err
exit 0
```
* Follow the [mail setup](https://wiki.sapsailing.com/wiki/info/landscape/mail-relaying#setup-central-mail-server-instance-webserver) instructions
* Install the backup.sh script with the following contents:
```
#!/bin/sh

# This is a template for creating backup of data and persist them
# to a central backup server. Make sure to adapt this script to your needs.
#
# Maintainer: simon.marcel.pamies@sap.com

export HOME=/root

# Directories to backup
BACKUP_DIRECTORIES="/etc /home/trac/git /home/trac/mailinglists /home/trac/maven-repositories /home/trac/p2-repositories /home/trac/releases /home/trac/sapsailing_layouts.git /var/www/static /home/trac/crontab /home/scores /var/log/old"

# Prefix for backup - set that to the hostname of your server
# Make sure to change this!
PREFIX="webserver"

# Directory for temporary files
TARGET_DIR=/tmp

# Configuration for external backup server - needs a ssh key
BACKUP_SERVER="backup@172.31.25.136"

# Set date for this backup - this makes it possible to compare
# files from different branches
BACKUP_DATE=`date +%s`

# Aliases
BUP_CMD="/opt/bup/bup"
BUP_ADDITIONAL="-r $BACKUP_SERVER:/home/backup/$PREFIX"
BUP_IGNORES='--exclude-rx=/war/$ --exclude-rx=/cache/unique-ips-per-referrer/stats/'
BUP_CMD_INDEX="$BUP_CMD index $BUP_IGNORES"
BUP_CMD_SAVE="$BUP_CMD save --date=$BACKUP_DATE $BUP_ADDITIONAL"

PARAM=$@

# Make sure to init remote repository
$BUP_CMD init
ssh $BACKUP_SERVER "/opt/bup/bup init -r /home/backup/$PREFIX"

# Backup general directories
for OTHER_DIR in $BACKUP_DIRECTORIES; do
        NORMALIZED_DIR_NAME=${OTHER_DIR//\//-}
        $BUP_CMD_INDEX $OTHER_DIR
        $BUP_CMD_SAVE -n dir$NORMALIZED_DIR_NAME $OTHER_DIR
        # no need to remove any backup files here
done
```
* Register the backup script as a cron job by establishing the following `/etc/crontab` file:
```
SHELL=/bin/bash
PATH=/sbin:/bin:/usr/sbin:/usr/bin
MAILTO=simon.marcel.pamies@sap.com
HOME=/

# For details see man 4 crontabs

# Example of job definition:
# .---------------- minute (0 - 59)
# |  .------------- hour (0 - 23)
# |  |  .---------- day of month (1 - 31)
# |  |  |  .------- month (1 - 12) OR jan,feb,mar,apr ...
# |  |  |  |  .---- day of week (0 - 6) (Sunday=0 or 7) OR sun,mon,tue,wed,thu,fri,sat
# |  |  |  |  |
# *  *  *  *  * user-name command to be executed
0 22 * * * root /opt/backup.sh
```
* Install Wordpress
* Install gollum Wiki
* Copy git contents of ssh://trac@sapsailing.com/home/trac/git to /home/trac/git
* Ensure there is a /home/scores directory with subdirectories `barbados`, `kiwo`, `sailwave`, `scores`, `velum`, and `xrrftp`.
* Establish the Apache web server configuration, in particular ensure that the SSL certificates are in place (see [here](https://wiki.sapsailing.com/wiki/info/security/ssl-support)) and the following files are set up: `/etc/httpd/conf/httpd.conf`, `/etc/httpd/conf/passwd.awstats`, `/etc/httpd/conf/passwd.git`, and `/etc/httpd/conf/conf.d/*.conf`.
* Update the hostname in `/etc/sysconfig/network`: `HOSTNAME=analytics-webserver`
* Run `chkconfig sendmail off; chkconfig postfix on` to make sure that the postfix mail server is the one that will be launched during boot
* Reboot the system, among other things for the hostname change to take effect, and in addition to see whether all services start properly
* configure fail2ban by editing `/etc/fail2ban/jail.conf`, entering reasonable e-mail configuration for the `ssh-iptables` filter as follows:
```
[ssh-iptables]

enabled  = true
filter   = sshd
action   = iptables[name=SSH, port=ssh, protocol=tcp]
           sendmail-whois[name=SSH, dest=axel.uhl@sap.com, sender=fail2ban@sapsailing.com]
logpath  = /var/log/secure
maxretry = 5
```
* Ensure that fail2ban will be started automatically when the instance starts: `chkconfig --level 23 fail2ban on` and start it right away with `service fail2ban start`. You can see which filters are active using `service fail2ban status`.

## Appendix / Resources
BACKUP_DIRECTORIES="/etc /home/trac/git /home/trac/mailinglists /home/trac/maven-repositories /home/trac/p2-repositories /home/trac/releases /home/trac/sapsailing_layouts.git /var/www/static /home/trac/crontab /home/scores /var/log/old"

