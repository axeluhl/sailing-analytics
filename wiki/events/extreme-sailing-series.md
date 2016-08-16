# Specifica for the Extreme Sailing Series

[[_TOC_]]

The Extreme Sailing Series is a hospitality event that aims to make invitees participate as close as possible at a sailing race. To achieve this goal the races take place in front of a tribune placed directly at the water (called Stadium Racing). Invitees can be assigned to a boat crew and that way be part of a race like it is not possible in any other series. 

Each race has a course that is set that way that spectators can see as much as possible regardless of the wind direction (upwind start not to be implied). The event is set to span a week, racing happens on 4 or 5 days. Every day up to 10 races take place depending on the wind conditions. Race course is defined such that a race lasts (under normal conditions) not longer than 20 minutes.

Usually not more than 8 competitors race against each other. One of the competitors is always an invitational team from the local spot that is allowed to race.

## The pitch - SAP sponsoring the Extreme Sailing Series

Like businesses, sailors need the latest information to make strategic decisions - but they need it even faster. One wrong tack, a false estimation of the current, or the slightest wind shift can cost the skipper the entire race. As technology sponsor of the Extreme Sailing Series, SAP is showing how innovative IT solutions providing real time data analysis can give teams the competitive edge and make series run simpler.

SAP is at the center of today’s technology revolution, developing innovations that not only help businesses run like never before, but also improve the lives of people everywhere. As market leader in enterprise application software, SAP SE (NYSE: SAP) helps companies of all sizes and industries run better. From back office to boardroom, warehouse to storefront, desktop to mobile device – SAP empowers people and organizations to work together more efficiently and use business insight more effectively to stay ahead of the competition. SAP applications and services enable more than 258,000 customers to operate profitably, adapt continuously, and grow sustainably.

## External Links

* [Extreme Sailing Series Offical Website](http://www.extremesailingseries.com/)
* [Official Results](http://www.extremesailingseries.com/results)
* [Analytics Homepage](http://ess40-2015.sapsailing.com/)
* [Youtube Channel](http://www.youtube.com/user/ExtremeSailingSeries)

## Official information

The current notice of race and other information can be accessed on the following page: https://octpftp.egnyte.com/. The username is `ess_main` and the password can be retrieved from Simon or Clem.

## Boats
To achieve the goal of providing invitees an exciting event a new type of boats have been designed. Capable of reaching speeds usually reserved to motorboats even in medium wind conditions, the Extreme 40 has been designed by Olympic champions Yves Loday and Mitch Booth, with the aim to provide the international sailing arena with a visually stunning and 100% performance-focused multihull. 

<img src="/wiki/images/ESSSAPBoat.jpg" height="325px" width="600px"/>

Flying a hull in as little as 8 knots of breeze (15 kph), the 40-foot (12m) long carbon speed machine requires coordination, finesse but also sheer muscular power from the crews who battle it out. The generous sail area allows the Extreme 40s to sail faster than the wind, which might seem puzzling at first - in just 15 knots of wind, an Extreme 40 is capable of traveling at over 25 knots

## Scoring
Each race is scored using a high point system where the winner gets 10 points, the second gets 9 and so on (going not further than 3 points). At the end of an event the last race points get doubled (20 for the winner). If there is a tie break between two competitors then the last race sets the winner (breaks tie break). Points from each race are accumulated and result in the overall score for an event.

In addition to the overall leaderboard specific to an event a global leaderboard is being maintained that denotes positions for all events during a year. This scoring scheme used for the global leaderboard has the same rules as an event specific leaderboard. The winner of an event gets 10 points and so on.

<img src="/wiki/images/ESSLeaderboardMuscat.jpg"/>

Scoring can be altered by the race committee using standard rules like DNS (DidNotStart), DNF (DidNotFinish), DNC (DidNotCount) or RDG (RedressGiven). These rules (except the last one) usually lead to a score of 0 for the race.

### Group Racing
There is a special format where either a group of 6 or 8 boats race against each other. This format is applied whenever the conditions do not allow 10 or more boats to race on the selected race area.

* Group of 6: 2 groups of 6 boats each will race agains each other. The winner will get 8 points, the second 2 points and so on.
* Group of 8: Three groups A,B and C with 4 competitors each will race against each other. There will always be two groups racing together so that the fleet size is 8.

If group racing is announced then make sure to select the right scoring scheme that is called `HighPoint, winner gets 10 points (or 8)`. In order to inform the Sailing Analytics about a race being a group race, there is an activator in the race committee app that allows the race committee to mark a race as a group race. In the background the server will add a new event to the RaceLog that is called `AdditionalScoringInformation`.

## Event Setup
The setup for such an event usually consists of the following departments:

* Race Committee
  * Boat on the water
  * On the beach (Shore Control)
* Commentator
  * For online streaming and video (beach)
  * Live (beach)
* Video and Television
  * Cameras (water)
  * Camera (beach)
  * Tech team (Controller, Cutter, Streamer)
* Visualization
  * Provider for GPS fixes, course layout and competitor names (TracTrac)
  * 3D Visualization provider (BeTomorrow)
  * Wind information (SAP)
  * Live and official result provider (SAP Sailing Analytics)

## Roles

* OC Sport (Event Organizer)
  * Clementine d'Oiron (Senior SAP Account Manager)
  * Jonathan Meadowcroft (CTO, in charge of IT and network infrastructure)
  * Liam Lavers (IT Support)
* GMR (SAP Lounge)
  * Mark Angell (Senior SAP Account Manager)
  * Nick Houchin (SAP Account Manager, Host Manager)
  * Milan Cerny (Business Consultant, Media Contact)
* TracTrac
  * Jakob Oedum (Senior Event Manager)
  * Jorge Llodra (IT Specialist)
* Sunset & Vine APP (TV)
  * Sarah Greene (Event Manager)
* SAP
  * Simon Pamies (Senior Development Architect, SAP ESS Project Lead)
  * Tom Peel (Technology Support)

## Team Contact Information

<pre>
The Wave	Rob Wilson	rob@robwilsonsailing.com
Team Russia	Alberto Barovier	abarovier@gmail.com
Red Bull	Nick Blackman	njblackman@gmail.com
SAP	Michael Hestbek	michael.hestbek@sapextremesailing.com
Oman Air	Rob Wilson	rob@robwilsonsailing.com
GAC Pindar	Tyson Lamond	tysonlamond@me.com
Team Turx	Edhem Dirvana	edhemdirvana@gmail.com
</pre>

## Competitor colors for 2015

<pre>
The Wave #16A6ED
Oman Air #B07A00
Gazprom Team Russia #FFFFFF
Lino Sonego Team Italia #000099
SAP Extreme Sailing Team #FFC61E
Invite #FFFF00
Beko Team Turx #FF0000
GAC Pindar #2AFFFF
Red Bull Sailing Team #990099
</pre>

## Equipment

* **From WDF**
  * 4x SIM Cards (2x WindBot, Spares)
  * 1x World Adapter
* **In Container**
 * 2x Samsung Tablet
 * 2x WindBots
 * 1x Satellite Modem
 * 1x Plug Strip
* **Provided by OC**
 * 1x Rack with servers and switches
 * 2x radio

## Technical Architecture
The technical infrastructure can be divided into two major parts.

### ON-PREMISE
This part physically consists of a rack that holds all servers needed to run the event in terms of tracking and network setup. All data (gps, wind, ...) is gathered here and then replicated to the cloud.

 * The **DNS-Server** serves not only as a Gateway to the internet but also has a nameserver installed that redirects some urls to the local setup. In addition to that a caching proxy makes sure that redundant bits of information are cached.
 * The **Sailing Analytics** server holds an Apache webserver that answers to *.sapsailing.com requests and of course the Java application running the analytics itself.
 * A **TracTrac** server that receives tracking data and provides the API to the analytics server to get GPS fixes in realtime.
 * A machine running windows and delivering 2D for TV overlays
 * Last but not least a **Hot-Spare** server that can be used in case of failure of another server.

In addition to the servers some switches make sure to have enough space for connecting additional networks and machines. To power the whole thing in the not so unlikely event of a power failure two mid-size UPS give enough power to at least shut down the servers cleanly.

### CLOUD
The cloud consists of one or more Sailing Analytics servers that present the data to the general public not being on premise. The on-premise servers replicate their data constantly to the cloud.

### Detailed Setup
The following image depicts the setup that is currently implemented. It features a local setup where the dependency on a reliable and fast internet connection is minimized as much as possible.

The core of this setup is a server that not only hosts a SAP Sailing Analytics but also the TracTrac server. This way the distribution of analytical information is not dependent on the speed and bandwidth of the local internet connection. By adding a DNS server in front of this analytics server local requests can be directed to the local server even when guests use a public internet address (e.g. www.sapsailing.com). 

In case of a problem with the local server requests can be redirected to the external analytics server. This server is constantly fed with data by a replication channel that gets information bits from the local analytics server.

* The SAP Sailing Analytics server that is authoritative for computing the results is no longer in the cloud but installed on premise. That way it is no longer dependent on a replicated TracTrac server but gets data directly from local TracTrac server.
* TracTrac Server is integrated with SAP Sailing Analytics on two physical appliances. That eases maintenance and data exchange between SAP and TracTrac services.
* Every leaderboard related information is gathered by accessing the local server. That way the dependency from the internet is drastically mitigated. Everyone on site always gets the right information without delay.
* A routing server manages the DNS resolution and in case of a local failure is able to transparently redirect data to the cloud.
* Score corrections also are not longer dependent on the internet connection but get fed directly into the local server.

<img src="/wiki/images/ESSSetupSOLL.jpg"/>

To not be dependent on a shaky power source that can be restored by "wiggling pieces a little to fix the generator" it has been decided to introduce UPS that can fed important hardware with power up to half an hour. The following picture depicts a quick shot on how this looks:

<img src="/wiki/images/ESSSetupUPS.jpg"/>

### DNS Server Setup
The DNS Server acts as a gateway/router, caching proxy and nameserver. 

### Gateway
All traffic to and from the internet is going through this server. To achieve that goal it routes all internal connections by default to an interface called `em2`. This interface is in most cases connected to an external router and must be configured according to the specifications sent over by the ISP. The routing is achieved by first having the right configuration for the interface (example from Cardiff ISP):

<pre>
[root@SAPDNS ~]# more /etc/sysconfig/network-scripts/ifcfg-em2
DEVICE="em2"
BOOTPROTO="static"
HWADDR="D4:AE:52:C9:F4:78"
NM_CONTROLLED="no"
TYPE="Ethernet"
UUID="e72740f5-ce09-4d3f-becf-a58f605211db"
ONBOOT="yes"
IPADDR=188.164.228.178
NETMASK=255.255.255.240
GATEWAY=188.164.228.177
DNS1=192.168.1.202
NAMESERVER=194.150.200.22
</pre>

Then the firewall needs to be configured to act as a router. There is a script at `/root/start_proxy.sh` that takes care of activating the right settings for the firewall but also for the proxy:

<pre>
#!/bin/sh
# Squid server IP
SQUID_SERVER="192.168.1.202"
# Interface connected to Internet
INTERNET="em2"
NETWORK="rename4"
MOBILE="p1p2"
# Address connected to LAN
LOCAL="192.168.1.0/24"
LOCAL2="192.168.4.0/24"
# Squid port
SQUID_PORT="3128"
# Clean old firewall
iptables -F
iptables -X
iptables -t nat -F
iptables -t nat -X
iptables -t mangle -F
iptables -t mangle -X
# Enable Forwarding
echo 1 > /proc/sys/net/ipv4/ip_forward
echo 1024 32768 > /proc/sys/net/ipv4/ip_local_port_range
echo 8192 > /proc/sys/net/ipv4/tcp_max_syn_backlog
ulimit -HSd unlimited
ulimit -HSn 16384
# Setting default filter policy
iptables -P INPUT DROP
iptables -P OUTPUT ACCEPT
# Unlimited access to loop back
iptables -A INPUT -i lo -j ACCEPT
iptables -A OUTPUT -o lo -j ACCEPT
# Allow UDP, DNS and Passive FTP
iptables -A INPUT -i $INTERNET -m state --state ESTABLISHED,RELATED -j ACCEPT
iptables -A INPUT -i $NETWORK -m state --state ESTABLISHED,RELATED -j ACCEPT
iptables -A INPUT -i $MOBILE -m state --state ESTABLISHED,RELATED -j ACCEPT
# set this system as a router for Rest of LAN
iptables -t nat -A POSTROUTING -o $INTERNET -j MASQUERADE
iptables -t nat -A POSTROUTING -o $NETWORK -j MASQUERADE
iptables -t nat -A POSTROUTING -o $MOBILE -j MASQUERADE
iptables -A FORWARD -s $LOCAL -j ACCEPT
iptables -A FORWARD -s $LOCAL2 -j ACCEPT
# unlimited access to LAN
iptables -A INPUT -s $LOCAL -j ACCEPT
iptables -A OUTPUT -s $LOCAL -j ACCEPT
iptables -A INPUT -s $LOCAL2 -j ACCEPT
iptables -A OUTPUT -s $LOCAL2 -j ACCEPT
# DNAT port 80 request comming from LAN systems to squid 3128 ($SQUID_PORT) aka transparent proxy
iptables -t nat -A PREROUTING -s $LOCAL -p tcp --dport 80 -j DNAT --to $SQUID_SERVER:$SQUID_PORT
# if it is same system
iptables -t nat -A PREROUTING -i $INTERNET -p tcp --dport 80 -j REDIRECT --to-port $SQUID_PORT
#open everything
iptables -A INPUT -i $INTERNET -j ACCEPT
iptables -A OUTPUT -o $INTERNET  -j ACCEPT
iptables -A INPUT -i $NETWORK -j ACCEPT
iptables -A OUTPUT -o $NETWORK  -j ACCEPT
iptables -A INPUT -i $MOBILE -j ACCEPT
iptables -A OUTPUT -o $MOBILE  -j ACCEPT
# udp DNS
iptables -A OUTPUT -p udp --dport 53 -m state --state NEW,ESTABLISHED -j ACCEPT
iptables -A INPUT -p udp --sport 53 -m state --state NEW,ESTABLISHED -j ACCEPT
# tcp DNS
iptables -A OUTPUT -p tcp --dport 53 -m state --state NEW,ESTABLISHED -j ACCEPT
iptables -A INPUT -p tcp --sport 53 -m state --state NEW,ESTABLISHED -j ACCEPT
# DROP everything and Log it
iptables -A INPUT -j LOG
iptables -A INPUT -j DROP
echo "Started firewall"
rndc querylog
echo "Started query logging for named"
</pre>

On interface `p1p2` in most cases there is a 4G router connected that provides an internet connection in case the main internet on `em2` is failing. In case of a failure on `em2` it does make sense to only provide internet to selected clients because the bandwidth on the backup line is very limited. In order to achieve this there is a script at `/root/activate_4gmodem.sh` that can be used to reroute some clients to the 4g line. This script needs the `iproute` package installed.


<pre>
[root@SAPDNS ~]# more configuration 
# Hosts that are routed via 4G modem
MODEM_4G_HOSTS="192.168.1.184 192.168.1.201"

[root@SAPDNS ~]# more activate_4gmodem.sh 
source /root/configuration
echo "Activating redirection for hosts $MODEM_4G_HOSTS to 4G modem (192.168.200.1)..."
ip route add default via 192.168.200.1 dev p1p2 table 4gmodem
for host in $MODEM_4G_HOSTS; do
    echo "  $host"
    ip rule add from $host lookup 4gmodem prio 1000
done
ip route flush table 4gmodem
ip route flush table main
sh /root/restart_firewall.sh

[root@SAPDNS ~]# more deactivate_4gmodem.sh 
source /root/configuration
echo "Removing all configured connections to 4G modem line..."
for host in $MODEM_4G_HOSTS; do
    echo "  $host"
    ip rule del from $host
done
ip route flush table 4gmodem
ip route flush table main
sh /root/restart_firewall.sh
</pre>

### Nameserver
The nameserver makes sure to redirect some urls to the local server. In the current configuration it will redirect all ess40-2014.sapsailing.com requests to local servers unless disabled. The basic configuration can be found in `/etc/named.conf`

<pre>
options {
	listen-on port 53 { 192.168.1.202; 192.168.4.202; };
	listen-on-v6 port 53 { ::1; };
	directory 	"/var/named";
	dump-file 	"/var/named/data/cache_dump.db";
        statistics-file "/var/named/data/named_stats.txt";
        memstatistics-file "/var/named/data/named_mem_stats.txt";
	allow-query     { any; };
	recursion yes;

	dnssec-enable yes;
	dnssec-validation yes;
	dnssec-lookaside auto;

	/* Path to ISC DLV key */
	bindkeys-file "/etc/named.iscdlv.key";

	managed-keys-directory "/var/named/dynamic";
};
logging {
        channel default_debug {
                file "data/named.run";
                severity dynamic;
        };
};
zone "." IN {
	type hint;
	file "named.ca";
};
zone "0.0.127.in-addr.arpa" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "localhost.db";
};
zone "1.168.192.in-addr.arpa" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "1.168.192.in-addr.arpa.db";
};
zone "4.168.192.in-addr.arpa" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "4.168.192.in-addr.arpa.db";
};
zone "portal.extremesailingseries.com" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "portal.extremesailingseries.com.db";
};

zone "www.sapsailing.com" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "sapsailing.com.db";
};
zone "ess40-2014.sapsailing.com" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "sapsailing.com.db";
};

zone "live.ess40-2014.sapsailing.com" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "sapsailing.com.db";
};
zone "ess40-mobile.sapsailing.com" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "sapsailing.com.db";
};
zone "ess40.local.sapsailing.com" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "sapsailing.com.db";
};
zone "live1.local.sapsailing.com" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "sapsailing.com.db";
};
zone "live2.local.sapsailing.com" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "sapsailing.com.db";
};
zone "ocsportmobile" IN {
	type master;
	allow-query { any; };
	allow-update { none; };
	file "portal.extremesailingseries.com.db";
};
include "/etc/named.rfc1912.zones";
include "/etc/named.root.key";
</pre>

That basic configuration only specifies the urls the nameserver will answer requests. For each url you then need to configure the answer at `/var/named/sapsailing.com.db`. Make absolutely sure to end each url entry with a dot!

<pre>
$TTL 86400
@ 	IN 	SOA ns.ocsportmobile. admin.ocsportmobile. (
			201304131
			1H
			7200
			1D
			1H)
		NS 	ns.ocsportmobile.
		TXT	"ESS DNS"

ess40-2015.sapsailing.com.	A	192.168.1.201
live.ess40-2015.sapsailing.com.	A	192.168.1.201
ess40-2015.ru.sapsailing.com.	A	192.168.1.201
live.ru.ess40-2015.sapsailing.com.	A	192.168.1.201
ess40-mobile.sapsailing.com.	A	192.168.1.201
ess40-local.sapsailing.com.	A	192.168.1.201
ess40.local.sapsailing.com.	A	192.168.1.201
live1.local.sapsailing.com.	A	192.168.1.201
live2.local.sapsailing.com.	A	192.168.1.201
www.sapsailing.com.	        A	192.168.1.201
</pre>

The tool to show configuration and manage entries at runtime is called `rndc`. If you want to see stats then you have to dump them using `rndc stats`. They will then get dumped to `/var/named/data/named_stats.txt`.

### Caching Proxy
The transparent caching proxy (Squid) intercepts all connections on port 80 (configured by the firewall script that forwards all connections on port 80 to port 3128 where the proxy runs) and caches some information bits like images or stylesheets. This helps reducing the bandwith. The configuration can be found at `/etc/squid/squid.conf`. The configuration is optimized for high traffic - make sure you understand what you're doing before changing anything.

<pre>
http_port 3128 transparent
cache_mem 2048 MB
cache_store_log none
cache_swap_high 100%
cache_swap_low 80%
half_closed_clients off
maximum_object_size 512 KB
memory_pools on
memory_pools_limit 2048 MB
maximum_object_size_in_memory 512 KB
ipcache_size 16000
ipcache_low 90
ipcache_high 97
debug_options ALL,2
dns_nameservers 192.168.1.202
positive_dns_ttl 8 hours
negative_dns_ttl 120 seconds
fqdncache_size 10000
#hierarchy_stoplist cgi-bin ?
#acl QUERY urlpath_regex cgi-bin \?
#cache deny QUERY
acl ads dstdomain "/etc/squid/ads.txt"
http_access deny ads
acl apache rep_header Server ^Apache
access_log /var/log/squid/access.log squid
hosts_file /etc/hosts
refresh_pattern ^ftp: 1440 20% 10080
refresh_pattern ^gopher: 1440 0% 1440
refresh_pattern -i (/cgi-bin/|\?.+) 0 0% 0
refresh_pattern .*?\?$ 14400 50% 18000 override-expire override-lastmod reload-into-ims ignore-reload ignore-no-cache ignore-private ignore-auth
refresh_pattern -i \.jpg$ 14400 50% 18000 override-expire override-lastmod reload-into-ims ignore-reload ignore-no-cache ignore-private ignore-auth
refresh_pattern -i \.gif$ 14400 50% 18000 override-expire override-lastmod reload-into-ims ignore-reload ignore-no-cache ignore-private ignore-auth
refresh_pattern -i \.png$ 14400 50% 18000 override-expire override-lastmod reload-into-ims ignore-reload ignore-no-cache ignore-private ignore-auth
refresh_pattern -i \.css$ 14400 50% 18000 override-expire override-lastmod reload-into-ims ignore-reload ignore-no-cache ignore-private ignore-auth
refresh_pattern -i \.js$ 14400 50% 18000 override-expire override-lastmod reload-into-ims ignore-reload ignore-no-cache ignore-private ignore-auth
refresh_pattern -i \.woff$ 14400 50% 18000 override-expire override-lastmod reload-into-ims ignore-reload ignore-no-cache ignore-private ignore-auth
refresh_pattern -i . 0 20% 4320
acl all src all
#0.0.0.0/0.0.0.0
acl manager proto cache_object
acl localhost src 127.0.0.1/32
acl to_localhost dst 127.0.0.0/8
acl lan src 192.168.1.0/24
acl lan src 192.168.4.0/24
acl doNotCacheDestinationIP dst 192.168.1.201 192.168.1.203
acl doNotCacheDomains dstdomain .sapsailing.com
acl SSL_ports port 443 563 # https, snews
acl SSL_ports port 873 # rsync
acl Safe_ports port 80 # http
acl Safe_ports port 21 # ftp
acl Safe_ports port 443 563 # https, snews
acl Safe_ports port 70 # gopher
acl Safe_ports port 210 # wais
acl Safe_ports port 1025-65535 # unregistered ports
acl Safe_ports port 280 # http-mgmt
acl Safe_ports port 488 # gss-http
acl Safe_ports port 591 # filemaker
acl Safe_ports port 777 # multiling http
acl Safe_ports port 631 # cups
acl Safe_ports port 873 # rsync
acl Safe_ports port 901 # SWAT
acl purge method PURGE
acl CONNECT method CONNECT
http_access allow manager localhost
http_access deny manager
http_access allow purge localhost
http_access deny purge
http_access deny !Safe_ports
http_access deny CONNECT !SSL_ports
http_access allow localhost
http_access allow localhost
http_access allow lan
http_access deny all
http_reply_access allow all
cache deny doNotCacheDestinationIP
cache deny doNotCacheDomains
icp_access allow all
visible_hostname ocsportmobileproxy
always_direct allow all
coredump_dir /var/spool/squid
</pre>

There is a web interface that is configued in `/etc/httpd/conf.d/squid.conf` that enables you to access some stats from http://192.168.1.202/Squid/cgi-bin/cachemgr.cgi. 

## Backup

There exists a backup of all data and configurations (DNS, TracTrac, SAP Analytics) on a USB harddisk that can be found in the suitcase that holds the tablets and other stuff.

## Additional information bits

### Useful tools to get information about system

* (Traffic) iptraf
* (CPU) htop
* (DHCP) dhcpstatus -s 192.168.1.0 | grep Active
* (General) multitail /var/log/messages /var/log/squid/access.log /var/log/squid/cache.log /var/named/data/named.run
* (Caching) squidclient -h 192.168.1.202 cache_object://localhost/ mgr:utilization
* (Caching) squidclient -h 192.168.1.202 cache_object://localhost/ mgr:info
* (Caching) http://192.168.1.202/Squid/cgi-bin/cachemgr.cgi?host=localhost&port=3128&user_name=&operation=counters&auth=

### Opening replication channel for Sailing Analytics server

The local server acts as a master and replicates data through a RabbitMQ running locally to one or more external servers, depending on the load balancing, DNS and cross-region configuration. The external server(s) need(s) to connect to localhost on 9087 (for live1) or 9088 (for live2) and can use the default RabbitMQ port (specifying "0" which maps to the default port 5672) on their localhost.

To launch the SSH tunnels, execute the following script from the `sailing` user's home directory, providing the IP addresses of the external servers as parameters:

<pre>
./ssh_tunnels_to_replicas.sh 54.76.64.42 54.72.14.139 54.169.167.42 54.169.157.202
</pre>

Make sure that you have an unlocked SSH key in memory (e.g., using a local ssh-agent or passing through with the `-A` option for your ssh connection to the live server) that is good for logging in on the external servers as user `sailing`. You should once do this for each of the servers whose IP addresses you specify:

<pre>
ssh sailing@54.76.64.42
</pre>

to ensure that the external server's host key can interactively be accepted and stored in the `known_hosts` file. Otherwise, the ssh connections will fail.

Check that all connections have been established, e.g., issuing the command

<pre>
ps axlw | grep ssh
</pre>

To terminate the tunnel connections, run the following command from the `sailing` user's home directory:

<pre>
./kill_ssh_tunnels_to_replicas.sh
</pre>

This kill script is generated during the execution of the script establishing the tunnels and records the PIDs of the autossh processes to kill.

### Replicate Race Logs

<pre>
#/bin/bash
EVENT_NAME="ESS 2014 Qingdao (Extreme40)"
REMOTE_USER=mongodb
REMOTE_SERVER=54.246.250.138
PARAM=$EVENT_NAME

echo "Exporting RACE_LOGS for $PARAM"
mongoexport --port 10201 -d winddb -c RACE_LOGS -q "{\"RACE_LOG_IDENTIFIER.a\" : \"$PARAM\"}" > race_logs.json
mongoexport --port 10201 -d winddb -c LEADERBOARDS -q "{\"REGATTA_NAME\" : \"$PARAM\"}" > leaderboards.json
ssh $REMOTE_USER@$REMOTE_SERVER 'rm /tmp/*.json'
scp race_logs.json $REMOTE_USER@$REMOTE_SERVER:/tmp 
scp leaderboards.json $REMOTE_USER@$REMOTE_SERVER:/tmp 
echo "Saved race logs and leaderboard"
ssh $REMOTE_USER@$REMOTE_SERVER '/opt/mongodb-linux-x86_64-1.8.1/bin/mongoimport --port 10201 --upsert -d winddb -c RACE_LOGS < /tmp/race_logs.json' 
ssh $REMOTE_USER@$REMOTE_SERVER '/opt/mongodb-linux-x86_64-1.8.1/bin/mongoimport --port 10201 --upsert -d winddb -c LEADERBOARDS < /tmp/leaderboards.json' 
echo "Finished"
</pre>