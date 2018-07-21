﻿# Welcome to the SAP Sailing Wiki

This is the <img src="https://www.sapsailing.com/images/sap-logo_grey.png"/> Wiki where useful information regarding this project can be found.

### The Pitch

Like businesses, sailors need the latest information to make strategic decisions - but they need it even faster. One wrong tack, a false estimation of the current, or the slightest wind shift can cost the skipper the entire race. As premium sponsor of the Kieler Woche 2011, and co-sponsor of Sailing Team Germany (STG), SAP is showing how innovative IT solutions providing real time data analysis can give teams the competitive edge.

SAP is at the center of today’s technology revolution, developing innovations that not only help businesses run like never before, but also improve the lives of people everywhere. As market leader in enterprise application software, SAP SE (NYSE: SAP) helps companies of all sizes and industries run better. From back office to boardroom, warehouse to storefront, desktop to mobile device – SAP empowers people and organizations to work together more efficiently and use business insight more effectively to stay ahead of the competition. SAP applications and services enable more than 258,000 customers to operate profitably, adapt continuously, and grow sustainably.

# Table of Contents

## Information

### General

* [[Information about this Wiki and HowTo|wiki/info/general/wiki]]
* [[General Project Information|wiki/info/general/general-information]]
* [[Inventory|wiki/info/general/inventar-liste]]

### Landscape and Development

* [[Architecture and Infrastructure|wiki/info/landscape/architecture-and-infrastructure]]
  * [[Runtime Environment|wiki/info/landscape/runtime-environment]]
  * [[Basic architectual principles|wiki/info/landscape/basic-architectural-principles]]
  * [[User Management|wiki/info/landscape/usermanagement]]
  * [[Development Environment|wiki/info/landscape/development-environment]]
  * [[Production Environment|wiki/info/landscape/production-environment]]
  * [[Typical Development Scenarios|wiki/info/landscape/typical-development-scenarios]]
* [[RaceLog Tracking Server Architecture|wiki/info/landscape/server]]
  * Environment Overview [[PDF|wiki/info/mobile/event-tracking/architecture.pdf]] | [[SVG|wiki/info/mobile/event-tracking/architecture.svg]]
* Amazon
  * [[Amazon EC2|wiki/info/landscape/amazon-ec2]]
  * [[EC2 Backup Strategy|wiki/info/landscape/amazon-ec2-backup-strategy]]
  * [[Creating an EC2 image from scratch|wiki/info/landscape/creating-ec2-image-from-scratch]]
  * [[Creating a webserver EC2 image from scratch|wiki/info/landscape/creating-ec2-image-for-webserver-from-scratch]]
  * [[EC2 mail relaying|wiki/info/landscape/mail-relaying]]
  * [[Setting up dedicated S3 buckets|wiki/info/landscape/s3-bucket-setup]]
* [[Building and Deploying|wiki/info/landscape/building-and-deploying]]
* [[Data Mining Architecture|wiki/info/landscape/data-mining-architecture]]
* [[Typical Data Mining Scenarios|wiki/info/landscape/typical-data-mining-scenarios]]
* [[Create clickable UI prototypes with Axure|wiki/info/landscape/ui-clickable-prototypes]]
* [[Webdesign|wiki/info/landscape/webdesign]]

### Mobile

* [[Mobile Development|wiki/info/mobile/mobile-development]]
* Tracking App
  * [[Tracking App Specification|wiki/info/mobile/app-spec/app-spec]]
  * [[Event Tracking|wiki/info/mobile/event-tracking/event-tracking]]
  * [[Steps for setting up Smartphone Tracking|wiki/info/mobile/smartphone-tracking-steps]]
  * [[Tracking App Prototype Architecture|wiki/info/mobile/app]]
* Racecommittee App
  * [[Racecommittee App|wiki/info/mobile/racecommittee-app]]
  * [[Environment|wiki/info/mobile/racecommittee-app-environment]]
  * [[Administrator|wiki/info/mobile/racecommittee-app-administrator]]
  * [[User|wiki/info/mobile/racecommittee-app-user]]
* [[Android and Release Build|wiki/info/mobile/android-and-release-build]]
* [[Energy consumption of mobile apps|wiki/info/mobile/energy-consumption]]
* [[Data consumption of mobile apps|wiki/info/mobile/data-consumption]]
* [[Mobile Sailing Analytics|wiki/info/mobile/mobilesailinganalytics]]
* [[Push Notifications|wiki/info/mobile/push-notifications]]
* [[NMEA|wiki/info/mobile/nmea]]

### API

* [[Web Services API|wiki/info/api/sailing-webservices]]
* [[API v1|wiki/info/api/api-v1]]
* [[Tracking App API|wiki/info/api/tracking-app-api]]
* [[Training API v1 draft|wiki/info/api/training-api-v1-draft]]

### Security
  * [[Fortify Tests|wiki/info/security/fortify]]
  * [[SSL / HTTPS Support|wiki/info/security/ssl-support]]
  * [[Permission Concept|wiki/info/security/permission-concept]]

### Miscellaneous

* [[Big Data - Numbers|wiki/info/misc/big-data-numbers]]
* [[Data Quality|wiki/info/misc/data-quality]]
* [[Sailing Domain Algorithms|wiki/info/misc/sailing-domain-algorithms]]
* [[Google Analytics (Web Page Tracking)|wiki/info/misc/ganalytics]]
* [[S3 Development Sample|wiki/info/misc/s3-sample]]

* [[FAQ for League Operators|wiki/info/misc/league-operators-faq]]
* [[Prionet related information|wiki/info/misc/prionet-related-information]]
* [[TracTrac Workshop 2013|wiki/info/misc/tractracworkshop2013]]

## HowTo

* [[Onboarding|wiki/howto/onboarding]]
* [[Importing Sessions from Expedition|wiki/howto/expeditionimport]]
* [[Checking our DBs for a user record by e-mail|wiki/howto/privacy]]

### Development

* [[Create boat graphics for the 2D race viewer|wiki/howto/development/boatgraphicssvg]]
* [[Continuous Integration with Hudson/Jenkins|wiki/howto/development/ci]]
* [[Dispatch|wiki/howto/development/dispatch]]
* [[Working with GWT UI Binder|wiki/howto/development/gwt-ui-binder]]
* [[Java De(Serialization) and Circular Dependencies|wiki/howto/development/java-de-serialization-and-circular-dependencies]]
* [[JMX Support|wiki/howto/development/jmx]]
* [[Working with GWT Locally|wiki/howto/development/local-gwt]]
* [[Log File Analysis|wiki/howto/development/log-file-analysis]]
* [[Old Log Compression|wiki/howto/development/Log-File-Compression]]
* [[UI Tests with Selenium|wiki/howto/development/selenium-ui-tests]]
* [[Profiling|wiki/howto/development/profiling]]
* [[Working with GWT Super Dev Mode|wiki/howto/development/super-dev-mode]]

### For Event Managers

* [[Set up local network with replication server|wiki/howto/eventmanagers/event-network-with-replica]]
* [[Operating Igtimi WindBots|wiki/howto/eventmanagers/windbot-operations]]
* [[Linking Race Videos|wiki/howto/eventmanagers/linking-race-videos]]
* [[Import official results|wiki/howto/eventmanagers/results-import]]
* [[Pairing lists|wiki/howto/eventmanagers/pairing-lists]]
* [[Manage media content|wiki/howto/eventmanagers/Manage-media-content]]

### Setup

* [[Configure Races on Server|wiki/howto/setup/configure-races-on-server]]
* [[Setup local webserver to serve 360° videos|wiki/howto/setup/webserver/nginx-webserver]]
* [[Setting up internal Jenkins on SAP Monsoon|wiki/howto/setup/setting-up-jenkins-on-sap-monsoon]]

### Miscellaneous

* [[Cook Book|wiki/howto/misc/cook-book]]
* [[Polars|wiki/howto/misc/polars]]
* [[Server Replication|wiki/howto/misc/server-replication]]
* [[TracTrac|wiki/howto/misc/tractrac-lifecycle]]
* [[UI Tests|wiki/howto/misc/ui-tests-tutorial]]
* [[Uploading Media Content|wiki/howto/misc/uploading-media-content]]
* [[Monitoring Apache and RabbitMQ|wiki/misc/monitoring-apache-and-rabbitmq]]

## Projects
* [[Analytics on a stick|wiki/projects/analytics-on-a-stick]]

## Events and Planning
* [[Project Planning (bigger development)|wiki/events/planning]]
* [[General Event Planning|wiki/events/general-event-planning]]
* [[Information about Extreme Sailing Series|wiki/events/extreme-sailing-series]]
* [[Travem&uuml;nder Woche 2014 event page|wiki/events/tw2014]]
* [[505 worlds Kiel 2014 event page|wiki/events/505-worlds-kiel-2014]]
* [[Kieler Woche event page|wiki/events/kieler-woche-2015]]
* [[Charleston Race Week 2016|wiki/events/Charleston-Race-Week-2016]]
* [[Sailing Leagues 2016|wiki/events/sailing-Leagues-2016]]
* [[Media Content|wiki/events/Sailing-events-media-content]]

## Planning
* [[Overview|https://wiki.sapsailing.com/pages/wiki/planning/]]

## Internal services (not related to wiki but useful)

* [Bugzilla Issue Tracking System](http://bugzilla.sapsailing.com/bugzilla/)
* [GIT Repository (SAP)](ssh://git.wdf.sap.corp:29418/SAPSail/sapsailingcapture.git)
* [Maven Repository Browser](http://maven.sapsailing.com/maven/) (see [[how to setup repository for Android builds|wiki/info/mobile/racecommittee-app-environment]])
* [Main Sailing Website](http://www.sapsailing.com)
* [Visitor Statistics](http://analysis.sapsailing.com/)
