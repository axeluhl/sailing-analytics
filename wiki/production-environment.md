# Production Environment

[[_TOC_]]

## General

Our current server deployment uses a 64bit Java7 Hotspot virtual machine and runs on a 64bit Linux CentOS distribution. We have a single host (sapsailing.com) which runs a number of Java VMs, some to offer the application in different development stages (dev, test, prod, ...), some to perform specific tasks such as replicating UDP wind data to the various server processes, or a process to store data received from the SwissTiming connector durably while forwarding that data to a server VM requesting it.

The various processes run in "tmux" sessions to which, once connected to sapsailing.com with an ssh client, users can gain access using the `tmux -2 attach-session -t sailing` command. The tmux environment including is started automagically upon system boot.

For the OSGi containers by convention we have one directory under _/home/trac/servers/_ per deployable branch (dev, test, prod1, prod2). In those directories we have copies of the "install" script from the git's java/target folder. Running it after a successful product build on the branch corresponding to the current directory will copy the compiled product to the server directory. Running the start script will then launch the respective server instance. A safety check in the install script avoids accidentally overwriting a server directory with a non-matching product version by comparing the directory name with the branch name checked out under _/home/trac/git_.

## Configuration Files
Configuration files are coming from a git repository that is located at server:/home/trac/git-serverconfig. There is a clone at server:/home/trac/serverconfig/production. This clone is based on the branch production. Files from this directory are used as the base for all configuration files on the server. NEVER touch git-serverconfig (the ORIGIN).

If you want to make a small change without much impact, then edit files in server:/home/trac/serverconfig/production. Test your changes and then commit and push. Make sure to restart services as needed.

For bigger changes clone to your development environment or perform your work at /home/trac/serverconfig/master. Make sure master is in sync with production branch and perform and test your changes. Once you’re satisifed commit and push to master, merge and push to production, login as trac user onto server and update. Restart services as needed.

The directory layout allows you to add configuration for each server. This is needed because there are files that have server specific information contained. If there are files that are the same for every server then you can put them in the shared directory.

## Apache Configuration
Currently every HTTP request is first directed to an Apache server that listens on port 80. The configuration for it can be found in /etc/httpd/* and has two parts:

*	The first part, located at /etc/httpd/conf/*, contains general configuration. Here you can configure timeouts, ports, default error messages and many other settings. You find these in a file named httpd.conf. For a new server environment it is crucial to at least check the ServerName, NameVirtualHost and ErrorDocument directives. Other directives should not be changed.
*	The second part is domain specific and contains information on how to handle (sub-)domains pointing to that server. Each handler, for such a domain, is known as a VirtualHost. At /etc/httpd/conf/macros you’ll find basic definitions of such a VirtualHost. These definitions are prepared to be used in a parametrized way. There is no need to write these definitions by yourself. The configuration file /etc/httpd/conf.d/000-events.conf shows how to use these macros. Make sure to never rename this file as the naming is needed to make sure configuration is loaded correctly.
If you want to add a new (sub-)domain then you just need to add a new entry to the 000-events.conf file. Such an entry could look like this:
Use Spectator-PROD2 49-euros2012.sapsailing.com “49er European 2012”

Make sure to use the domain defined by the ServerName directive. Other domains could not work. There are currently three different types of macros that can be used:

1.	Leaderboard make a parametrized leaderboard view available
2.	Spectator yields a view suited for spectators
3.	Event generates specific views that contains information about an event

The current macro definition is prepared for GWT handling and redirects all other URLs directly to the Jetty server without interfering. You need to be aware of the fact, that all POST requests are altered by adding a new Header Cache-Control: no-cache, no-store to the HTTP header section.
For new server deployments make sure to adapt the macros file according to the network configuration. It is crucial to at least change the IP address otherwise directives won’t work.
