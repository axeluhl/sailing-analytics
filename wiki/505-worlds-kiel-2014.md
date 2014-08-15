# 505 Worlds 2014, Kiel

## Server Landscape

We run two servers for the event, A and B, for fast and available switching. The DB they use is called 505WORLDS2014, and so is the replication channel. The git branch we use to merge the features we want to deploy to the event servers is called `505worlds2014` and the git tag used to build a release for it is called `505worlds2014-release`. See http://hudson.sapsailing.com/job/SAPSailingAnalytics-505worlds2014/ and http://hudson.sapsailing.com/job/SAPSailingAnalytics-505worlds2014-release/, respectively, for their Hudson jobs.

## Replication

RabbitMQ server for replication: 54.246.250.138
Replication Exchange Name: 505WORLDS2014

Regatta Overview: http://505worlds2014ro.sapsailing.com

Manage2Sail Event URL: http://manage2sail.com/api/public/links/event/d70bee3e-9f65-46f4-96a1-9fd467712ce0?accesstoken=bDAv8CwsTM94ujZ&mediaType=json

Event Page (newhome): http://505worlds2014.sapsailing.com/gwt/Home.html#EventPlace:eventId=94e1e240-8a3d-466b-9f39-419f311eaace

### A server

Admin Console at http://ec2-54-77-58-65.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
External IP: 54.77.58.65
Internal IP: 172.31.30.45

### B server

Admin Console at http://ec2-54-77-33-52.eu-west-1.compute.amazonaws.com:8888/gwt/AdminConsole.html
External IP: 54.77.33.52
Internal IP: 172.31.31.62

## TracTrac URL

http://secondary.traclive.dk/events/event_20140813_Worlds/jsonservice.php

### Touch Computer (Lenovo) Setup 

#### Installation Browser & Setup

1. [ Firefox 32 Beta for best touch support - Download & Install ](https://www.mozilla.org/de/firefox/channel/#beta)

2. User Agent Firefox Addon OR set default UA of Firefox
 
 2.1. [ Firefox Addon User Agent Switcher 0.7.3 - Install](https://addons.mozilla.org/en-US/firefox/addon/user-agent-switcher/)

 2.2. [ User Agent Switcher - Import UAs (XML)](http://techpatterns.com/downloads/firefox/useragentswitcher.xml)

 2.3. Set UA: Tools -> Default User Agent -> Mobile Devices -> Browsers -> Firefox Fennec 10.0.1

 **alternatively**

 2.1. Change default UA [HowToGeek](http://www.howtogeek.com/113439/how-to-change-your-browsers-user-agent-without-installing-any-extensions/) to the one of a Samsung Galaxy Note Tablet with Chrome: 
<pre>
Mozilla/5.0 (Linux; Android 4.1.2; GT-N8000 Build/JZO54K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.166 Safari/537.36 OPR/20.0.1396.73172
</pre>

#### Installation Keyboard & Setup

1. [ Hot Virtual Keyboard - Download & Install](http://hot-virtual-keyboard.com/files/vk_setup.exe)
2. Hot Virtual Keyboard Settings:
  - Check Main -> Run at Startup
  - Check OnScreen Keyboard -> Show the on-screen keyboard when the text cursor is visible
  - Check OnScreen Keyboard -> Auto Hide
  - Optional: Check Word Autocomplete -> Enabled