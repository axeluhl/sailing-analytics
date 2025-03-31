# Production Environment

[[_TOC_]]

## General

Our current server deployment uses a 64bit Java7 Hotspot virtual machine and runs on a 64bit Linux CentOS distribution. We have a single host (sapsailing.com) which runs a number of Java VMs, some to offer the application in different development stages (dev, test, prod, ...), some to perform specific tasks such as replicating UDP wind data to the various server processes, or a process to store data received from the SwissTiming connector durably while forwarding that data to a server VM requesting it.

The various processes run in "tmux" sessions to which, once connected to the desired EC2 instance with an ssh client and becoming the sailing user 'su - sailing', users can gain access using the `tmux attach` command. The tmux environment including is started automagically upon system boot by invoking the script _/home/trac/servers/tmuxManagementConsole.sh_.

For the OSGi containers by convention we have one directory under _/home/trac/servers/_ per deployable branch (dev, test, prod1, prod2). In those directories we have copies of the "install" script from the git's java/target folder. Running it after a successful product build on the branch corresponding to the current directory will copy the compiled product to the server directory. Running the start script will then launch the respective server instance. A safety check in the install script avoids accidentally overwriting a server directory with a non-matching product version by comparing the directory name with the branch name checked out under _/home/trac/git_.

## Firewall
The current firewall configuration is described in the following table. Firewall is maintained by PIRONET NDH. In order to change the configuration you have to sign a form ([[Click here to open it|wiki/uploads/Firewall-Freischaltungformular-v33-de.doc]]).

<table>
<tr>
<th>Source</th>
<th>Target</th>
<th>Port</th>
<th>Type</th>
</tr>
<tr>
<td>ALL</td>
<td>sapcoe-app01</td>
<td>80</td>
<td>TCP</td>
</tr>
<tr>
<td>ALL</td>
<td>sapcoe-app01</td>
<td>443</td>
<td>TCP</td>
</tr>
<tr>
<td>ALL</td>
<td>sapcoe-app01</td>
<td>22</td>
<td>TCP</td>
</tr>
<tr>
<td>sapcoe-app01</td>
<td>ALL</td>
<td>123</td>
<td>TCP</td>
</tr>
<tr>
<td>ALL</td>
<td>sapcoe-app01</td>
<td>2010-1015</td>
<td>UDP</td>
</tr>
<tr>
<td>ALL</td>
<td>sapcoe-app01</td>
<td>21</td>
<td>TCP</td>
</tr>
<tr>
<td>ALL</td>
<td>sapcoe-app01</td>
<td>5672</td>
<td>TCP</td>
</tr>
<tr>
<td>sapcoe-app01</td>
<td>78.46.86.151</td>
<td>ALL</td>
<td>TCP+UDP</td>
</tr>
<tr>
<td>sapcoe-app01</td>
<td>46.4.29.12</td>
<td>ALL</td>
<td>TCP+UDP</td>
</tr>
<tr>
<td>sapcoe-app01</td>
<td>89.233.27.2</td>
<td>ALL</td>
<td>TCP+UDP</td>
</tr>
<tr>
<td>sapcoe-app01</td>
<td>gps.sportresult.com</td>
<td>40300</td>
<td>TCP+UDP</td>
</tr>
</table>

## Services

Services are vital for the infrastructure. The following sections describe location of configuration and special behaviour.

### Java Servers

To ensure availability and to be able to separate servers holding historical data and live data there are a bunch of preconfigured java servers.

<table>
<tr>
<th>Name</th>
<th>HTTP</th>
<th>MongoDB Port</th>
<th>MongoDB Path</th>
<th>Replication Channel</th>
<th>Expedition UDP Port</th>
<th>OSGi Telnet Port</th>
</tr>
<tr>
<td>DEV</td>
<td>8886</td>
<td>10200</td>
<td>/opt/mongodb/data/mongodb-dev</td>
<td>sapsailinganalytics-dev</td>
<td>2010</td>
<td>14886</td>
</tr>
<tr>
<td>LIVE1</td>
<td>8887</td>
<td>10201</td>
<td>/opt/mongodb/data/mongodb-live</td>
<td>sapsailinganalytics-live1</td>
<td>2011</td>
<td>14887</td>
</tr>
<tr>
<td>LIVE2</td>
<td>8888</td>
<td>10201</td>
<td>/opt/mongodb/data/mongodb-live</td>
<td>sapsailinganalytics-live2</td>
<td>2013</td>
<td>14888</td>
</tr>
<tr>
<td>ARCHIVE1</td>
<td>8889</td>
<td>10202</td>
<td>/opt/mongodb/data/mongodb-archive</td>
<td>sapsailinganalytics-archive1</td>
<td>N/A</td>
<td>14889</td>
</tr>
<tr>
<td>ARCHIVE2</td>
<td>8890</td>
<td>10202</td>
<td>/opt/mongodb/data/mongodb-archive</td>
<td>sapsailinganalytics-archive2</td>
<td>N/A</td>
<td>14890</td>
</tr>
<tr>
<td>RACECOMMITTEE_APP</td>
<td>7777</td>
<td>10201</td>
<td>/opt/mongodb/data/mongodb-live</td>
<td>sapsailinganalytics-racecommitteeapp</td>
<td>2010</td>
<td>14777</td>
</tr>
</table>

### Apache

Apache is listening to port 80 and is answering all HTTP requests. There are two configuration files that control the behaviour. 

* The first one, located at /etc/httpd/conf.d/000-events.conf, controls all sbdomains related to sailing events. This file uses macros that are defined in /etc/httpd/conf/macros. Using these macros you can associate a tracked race to a domain very easily. Each handler, for such a domain, is known as a VirtualHost. At /etc/httpd/conf/macros you’ll find basic definitions of such a VirtualHost. These definitions are prepared to be used in a parametrized way. There is no need to write these definitions by yourself. The configuration file /etc/httpd/conf.d/000-events.conf shows how to use these macros. Make sure to never rename this file as the naming is needed to make sure configuration is loaded correctly.
If you want to add a new (sub-)domain then you just need to add a new entry to the 000-events.conf file. Such an entry could look like this:
Use Spectator-PROD2 49-euros2012.sapsailing.com “49er European 2012”. The current macro definition is prepared for GWT handling and redirects all other URLs directly to the Jetty server without interfering. You need to be aware of the fact, that all POST requests are altered by adding a new Header Cache-Control: no-cache, no-store to the HTTP header section.
For new server deployments make sure to adapt the macros file according to the network configuration. It is crucial to at least change the IP address otherwise directives won’t work.

* The second one, located at /etc/httpd/conf.d/mainurl.conf hosts definitions for all services that are needed. These include Maven and P2 repositories and for instance the bugzilla domain.

* The third part, located at /etc/httpd/conf/*, contains general configuration. Here you can configure timeouts, ports, default error messages and many other settings. You find these in a file named httpd.conf. For a new server environment it is crucial to at least check the ServerName, NameVirtualHost and ErrorDocument directives. Other directives should not be changed.

After each change you need to reload apache by using `service httpd reload` from commandline.

Logfiles are located at /var/log/httpd. Every event and access to www.sapsailing.com goes to access_log. All other services like maven go to services_log file.

Errorpages for 404 or other errors can be found in /var/www/errorpages.

### Postfix

Postfix handles all email traffic. Its configuration can be found in /etc/postfix/main.cf. There should be no need to change this.

### Gollum

Gollum is the software behind our Wiki that is currently tied to http://wiki.sapsailing.com. Gollum is not under control of system package manager and therefore requires manual updates.

This software requires a running Ruby with minimal version 1.8.7. This version is only available by using the following repository:

<pre>
[webtatic]
name=Webtatic Repository $releasever - $basearch
#baseurl=http://repo.webtatic.com/yum/centos/5/$basearch/
mirrorlist=http://repo.webtatic.com/yum/centos/5/$basearch/mirrorlist
enabled=1
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-webtatic-andy
</pre>

Gollum is installed system-wide by using the command `yum install ruby rubygems` followed by a `gem install gollum`. The configuration for gollum can be found in /opt/gollum-ext/ where also additional sourcecode can be found that is needed for authentication.

<pre>
require 'rubygems'
require 'yaml'
require 'app'
App.set(:gollum_path, "/home/trac/git")
App.set(:authorized_users, YAML.load_file(File.expand_path('users.yml', __DIR__)))
App.set(:wiki_options, {:live_preview, false})
App.set(:default_markup, :markdown)
run App
</pre>

Authentication can be controlled by editing the file /opt/gollem-ext/users.yml. Passwords are encoded using sha1sum like this `echo -n mypassword | sha1sum`. Copy the generated hexdigest to the configuration file.

### Bugzilla

Bugzilla is the main issue management system and its code is located at /usr/share/bugzilla. Its code base is managed by package manager.

### Piwik

Piwik is a analytics software to get meaningful values out of log files. It needs a PHP version 5.3 with installed mysql extensions (pdo, mysqli). You also need to preconfigure a MySQL database. The configuration and code for Piwik is located at /var/www/piwik. Users are being stored in mysql database. If you want to add a new user then just login with admin user and create users using the GUI. 

Piwik is not under control of package management. Updates need to be performed manually.

At /etc/cron.d/piwik you can find the cron job definition for updating data. By default it uses a script located at /opt/piwik-scripts that looks like this:

<pre>
#!/bin/bash

ACTUAL_LINES=`cat /var/log/httpd/access_log | wc -l`
LINE_START=`cat /opt/piwik-scripts/last-line-count`

if [ $LINE_START -gt $ACTUAL_LINES ]; then
	echo "Resetting count"
	LINE_START=0
fi

echo "Line position: $LINE_START / $ACTUAL_LINES"

/home/web/Python-2.6.6/bin/python2.6 /var/www/piwik/misc/log-analytics/import_logs.py -d --exclude-path-from=/opt/piwik-scripts/EXCLUDE --url=http://analysi
s.sapsailing.com --skip=$LINE_START --enable-static --enable-bots --recorders=4 --recorder-max-payload-size=400 --add-sites-new-hosts /var/log/httpd/access_
log

echo $ACTUAL_LINES > /opt/piwik-scripts/last-line-count
</pre>

### MongoDB

MongoDB configuration can be found in /opt/mongodb/etc. This service is automatically started by the /etc/init.d/sailing script upon startup. Configuration for mongodb is maintained on every branch in the configuration/ directory.

### MySQL

MySQL serves as database backend for Piwik and Bugzilla. Configuration can be found in /etc/my.cnf and database files in /var/lib/mysql.