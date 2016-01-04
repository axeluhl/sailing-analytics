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

At this event we operate a replica on our SAP Sail Cube server. The server is physically located behind the ground floor desk. With one of its network adapters it is hooked up to the lounge network, using a radio link from realTV. The second network adapter (Mainboard) is connected to the cube's network, allowing access from studio PCs to the cube server. This adapter is also the default route, providing the cube server, cube showroom and realTV with Internet access. As a backup it is possible to switch the default route to the lounge. See `ip route show`.

The server runs a isc-dhcp-server, which requires all connected routers to NOT deliver a dhcp server. All required subnet config is in `/etc/dhcp/dhcpd.conf`. We have a /22 subnet for cube and a /24 subnet for the lounge. BeTomorrow get static ips assigned by the dhcp through MAC-addresses, because their server always needs the same ip address. Futhermore the dhcp deploys our own DNS server here. For further description of the DNS setup read further.

The server also runs a bind9 DNS server which is set as the primary DNS for all lounge and cube PCs. All DNS requests are forwarded to a public DNS server except for requests for 505worlds2014.sapsailing.com for which the local DNS server returns the IP address of the cube server for the NIC through which the DNS request was received. For that two DNS zones are introduced, which match by the incoming IP range request. See `/etc/bind/named.conf.zones-cube` and `/etc/bind/named.conf.zones-launch` for definition of zone and forwarding. Take a look at `/etc/bind/zones/505sapsailing.cube.db` and `/etc/bind/zones/505sapsailing.sailing.db` for the nameserver entries for the zone. The result is, that if a request comes from the lounge network, the domain is resolved to the ip the cube server has in this subnet and also if the request comes from the cube, it returns the ip in the subnet of the cube.
As described in bug2204 wind data and API are not served through the replica, so BeTomorrow has to go to our main server. For that I introduced a subsubdomain `btmw.505worlds2014.sapsailing.com` which points to `sapsailing.com` and answers with our main active server of the event.

On the cube server we run a replica server instance as user `sailing` under the usual directory set-up (`/home/sailing/servers/server`). The `env.sh` file uses a configuration that lets the server start replicating when launched. The two possible master servers (A / B) are specified in two lines defining `REPLICATE_MASTER_SERVLET_HOST`, one is commented, the other one active.

When switching from the A server to the B server or vice versa, the replica needs to be re-configured. Before stopping it, connected clients need to be re-directed to the master when they hit 505worlds2014.sapsailing.com. This is achieved by re-configuring the Apache server running on the replica, as user `root`, editing `/etc/apache2/sites-enabled/001-events.conf` and activating the line for `505worlds2014.sapsailing.com` that points to the IP address of the new active server, then invoking `/etc/init.d/apache2 reload`. Once this is done, the running replica should cleanly stop replication (using the `Replication` tab in the AdminConsole), then stopping the instance (as user `sailing` run `/home/sailing/servers/server/stop`). Then, in `env.sh` the `REPLICATE_MASTER_SERVLET_HOST` variable needs to be set to point to the new master. After that, the replica Java instance can be launched using `/home/sailing/servers/server/start`). Before re-activating it is important that the rabbit initial load is done. So please watch either `/home/sailing/servers/server/logs/sailing0.log.0` or the Rabbit Webinterface on `http://54.246.250.138:15672/#/queues`. Please deleted eventually displayed old queues (be sure not to remove the correct replicating queue!). Re-configure Apache again by editing `/etc/apache2/sites-enabled/001-events.conf` again, re-activating the record that lets 505worlds2014.sapsailing.com point to the replica. From this point on, clients in the lounge and the Cube will instantly be directed to the cube server again when hitting the replica server's Apache with a URL starting with `505worlds2014.sapsailing.com`. You can check this by example checking the load with `htop` or `apachetop`.

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