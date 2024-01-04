# Setting up an image for the hudson.sapsailing.com server

This is an add-on to the regular EC2 image set-up described [here](https://wiki.sapsailing.com/wiki/info/landscape/creating-ec2-image-from-scratch). An Android SDK needs to be installed.


* Create a ``hudson`` user/group
* Make sure ``/home/hudson`` is a separate mount; probably just mount the existing volume of a previous installation
* Install an Android SDK under ``/home/hudson/android-sdk-linux``; if you simply re-used an old ``/home/hudson`` mount this should already be in place.
* Install Eclipse to ``/home/hudson/eclipse`` to allow sharing it in case a large AWS instance is needed, e.g., for heap dump analysis.
* export ``/home/hudson/android-sdk-linux`` and ``/home/hudson/eclipse`` as follows in ``/etc/exports``:
```
/home/hudson/android-sdk-linux 172.31.0.0/16(rw,nohide,no_root_squash)
/home/hudson/eclipse 172.31.0.0/16(rw,nohide,no_root_squash)
```

* Ensure you have EC2 / EBS snapshot backups for the volumes by tagging them as follows: ``WeeklySailingInfrastructureBackup=Yes`` for ``/`` and ``/home/hudson``.

``/home/hudson/repo`` has the Hudson build repository. The Hudson WAR file is under ``/usr/lib/hudson/hudson.war`` which has to be taken from [here](https://static.sapsailing.com/hudson.war.patched-with-mail-1.6.2). ``/etc/init.d/hudson``, linked to from ``/etc/rc0.d/K29hudson``, ``/etc/rc1.d/K29hudson``, ``/etc/rc2.d/K29hudson``, ``/etc/rc3.d/S81hudson``, ``/etc/rc4.d/K29hudson``, ``/etc/rc5.d/S81hudson``, and  ``/etc/rc6.d/K29hudson``, takes care of spinning up Hudson during instance re-boot. It can be obtained from [here](https://static.sapsailing.com/etc-init.d-hudson). Hudson systemwide configuration is under ``/etc/sysconfig/hudson``:
```
## Path:        Development/Hudson
## Description: Configuration for the Hudson continuous build server
## Type:        string
## Default:     "/var/lib/hudson"
## ServiceRestart: hudson
#
# Directory where Hudson store its configuration and working
# files (checkouts, build reports, artifacts, ...).
#
HUDSON_HOME="/home/hudson/repo"

## Type:        string
## Default:     ""
## ServiceRestart: hudson
#
# Java executable to run Hudson
# When left empty, we'll try to find the suitable Java.
#

HUDSON_JAVA_CMD="/opt/sapjvm_8/bin/java"
# The following line choses JavaSE-1.7
#HUDSON_JAVA_CMD="/opt/jdk1.7.0_02/bin/java"
# The following line choses JavaSE-1.8
#HUDSON_JAVA_CMD="/opt/jdk1.8.0_20/bin/java"

## Type:        string
## Default:     "hudson"
## ServiceRestart: hudson
#
# Unix user account that runs the Hudson daemon
# Be careful when you change this, as you need to update
# permissions of $HUDSON_HOME and /var/log/hudson.
#
HUDSON_USER="hudson"

## Type:        string
## Default:     "-Djava.awt.headless=true"
## ServiceRestart: hudson
#
# Options to pass to java when running Hudson.
#
HUDSON_JAVA_OPTIONS="-Djava.awt.headless=true -Xmx2G -Dhudson.slaves.ChannelPinger.pingInterval=60 -Dhudson.slaves.ChannelPinger.pingIntervalSeconds=60 -Dhudson.slaves.ChannelPinger.pingTimeoutSeconds=60"

## Type:        integer(0:65535)
## Default:     8080
## ServiceRestart: hudson
#
# Port Hudson is listening on.
#
HUDSON_PORT="8080"

## Type:        integer(1:9)
## Default:     5
## ServiceRestart: hudson
#
# Debug level for logs -- the higher the value, the more verbose.
# 5 is INFO.
#
HUDSON_DEBUG_LEVEL="5"

## Type:        yesno
## Default:     no
## ServiceRestart: hudson
#
# Whether to enable access logging or not.
#
HUDSON_ENABLE_ACCESS_LOG="no"

## Type:        integer
## Default:     100
## ServiceRestart: hudson
#
# Maximum number of HTTP worker threads.
#
HUDSON_HANDLER_MAX="100"

## Type:        integer
## Default:     20
## ServiceRestart: hudson
#
# Maximum number of idle HTTP worker threads.
#
HUDSON_HANDLER_IDLE="20"

## Type:        string
## Default:     ""
## ServiceRestart: hudson
#
# Pass arbitrary arguments to Hudson.
# Full option list: java -jar hudson.war --help
#
HUDSON_ARGS=""
```
