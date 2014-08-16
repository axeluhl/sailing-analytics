# 505 Worlds 2014, Kiel

## Server Landscape

We run two servers for the event, A and B, for fast and available switching. The DB they use is called 505WORLDS2014, and so is the replication channel. The git branch we use to merge the features we want to deploy to the event servers is called `505worlds2014` and the git tag used to build a release for it is called `505worlds2014-release`. See http://hudson.sapsailing.com/job/SAPSailingAnalytics-505worlds2014/ and http://hudson.sapsailing.com/job/SAPSailingAnalytics-505worlds2014-release/, respectively, for their Hudson jobs.

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

## Replication

RabbitMQ server for replication: 54.246.250.138
Replication Exchange Name: 505WORLDS2014

Regatta Overview: http://505worlds2014ro.sapsailing.com

Manage2Sail Event URL: http://manage2sail.com/api/public/links/event/d70bee3e-9f65-46f4-96a1-9fd467712ce0?accesstoken=bDAv8CwsTM94ujZ&mediaType=json

Event Page (newhome): http://505worlds2014.sapsailing.com/gwt/Home.html#EventPlace:eventId=94e1e240-8a3d-466b-9f39-419f311eaace

## Replication to Cube Server

At this event we operate a replica on our SAP Sail Cube server. The server is physically located behind the ground floor desk. With one of its network adapters it is hooked up to the lounge network, using a radio link, which is also the default route, providing the cube server with Internet access. The second network adapter is connected to the cube's network, allowing access from studio PCs to the cube server.

The server runs a bind9 DNS server which is set as the primary DNS for all lounge and cube PCs. All DNS requests are forwarded to a public DNS server except for requests for 505worlds2014.sapsailing.com for which the local DNS server returns the IP address of the cube server for the NIC through which the DNS request was received.

On the cube server we run a replica server instance as user `sailing` under the usual directory set-up (`/home/sailing/servers/server`). The `env.sh` file uses a configuration that lets the server start replicating when launched. The two possible master servers (A / B) are specified in two lines defining `REPLICATE_MASTER_SERVLET_HOST`, one is commented, the other one active.

When switching from the A server to the B server or vice versa, the replica needs to be re-configured. Before stopping it, connected clients need to be re-directed to the master when they hit 505worlds2014.sapsailing.com. This is achieved by stopping the bind9 service, as user `root` invoking `/etc/init.d/bind9 stop`. If you need DNS access while bind9 is stopped, edit `/etc/resolv.conf` to use the local FritzBox as its DNS server instead of the local bind9 service. Remember to switch this back when re-activating the replica. Once this is done, the running replica should cleanly stop replication (using the `Replication` tab in the AdminConsole), then stopping the instance (as user `sailing` run `/home/sailing/servers/server/stop`). Then, in `env.sh` the `REPLICATE_MASTER_SERVLET_HOST` variable needs to be set to point to the new master. After that, the replica Java instance can be launched using `/home/sailing/servers/server/start`). Start bind9 again as user `root` by calling `/etc/init.d/bind9 start`. From this point on, clients in the lounge and the Cube will be directed to the cube server again when resolving `505worlds2014.sapsailing.com`.

## Switching Servers

For maximum availability we have two EC2 servers reserved for the event (see above). We can prepare the other server by installing a release, either by using the `refreshInstance.sh` script or by using `tmux attach` to go to a console and fetching from git and building and installing there. When done, fire up the server and load all races required. Test the server reasonably well.

Then switch the cube server replica to point to the new server. This process is described above.

Then, log on as `root` on `sapsailing.com`, go to `/etc/httpd/conf.d` and edit `001-events.conf` to update the 505worlds2014-related URLs. By convention, the inactive server URLs are suffixed with `-a` or `-b` name, respectively. When done editing, run `/etc/init.d/httpd reload` which will update the Apache configuration so that from that point on requests to `505worlds2014.sapsailing.com` will go to the new server. Convince yourself by looking at the logs under `/home/sailing/servers/server/logs/sailing0.log.0` that the correct server is now active. Afterwards, stop the old server's Java instance.

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