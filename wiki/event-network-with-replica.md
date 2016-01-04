# Setup local area network for an Sailing Event

## Hardware
- Cisco Meraki MX60W
- Sail Cube Server (2HE, Xeon E3-12XXv2, 32GB RAM, 120GB SSD)

## Software
- Setup router through https://dashboard.meraki.com (get access from Axel, Frank or Steffen)
- Server OS: Debian Wheezy

## Scenarios
### Standard FritzBox / ... network
If you have a given network setup (without own router) I would suggest the following solution:
The server has three network adapters to connect to multiple networks at the same time (e.g. Cube and Lounge,..). You could simply integrate the server into all networks and set up a static ip on each of the networks for the server. then simply configure the sailing instance to listen on all interfaces and that should be it. You could additionally use the replica's DNS server to also overwrite `event.sapsailing.com` URL's but therefore you need to tell each router of the network to serve the replica as DNS server to other LAN clients.

### Single WAN uplink (Meraki)
- Create 3 VLAN's with (VLAN-ID 10=>ADMIN,20=>CUBE,30=>GUEST) 
- Link VLAN's with physical ports on the router
- Create 3 WLAN SSID's and link each with a VLAN
- Create a splash page for guest wifi (Security appliance->Access control and Splash page)
- Create traffic shaping rules / policies to limit bandwidth on each of the VLAN's
  * give Cube network about 1600Kb/s down and 500Kb/s up
  * make Admin network unlimited
  * set guest network limit to 200Kb/s up and down
- Set up DHCP section to your desire
  * e.g. deploy replica server IP as DNS server for Cube network
  * give importand servers (replica, TriCaster, beTomorrow server a static ip lease in Cube network) => MAC's needed
- Create firewall rules to restrict access to special services (e.g. ssh on replica server)
- If needed create a Client VPN Server and choose a free subnet for it (L2TP over IPSec)

### Dual WAN uplink (Meraki)
`not yet tested, but possible`

### Dual WAN uplink (Meraki) with mobile LTE
`not yet tested, but possible`

## Prepare Replica Server
### Physically
The server is localted behind the ground floor desk in the 19" cabinet. The router could be set up in the same place, so that a physical link can be established from server to router (short ways for better latency). 

### Services & configs
- runs `bind9`, `apache2`, `isc-dhcp-server` (default: off), 2x `sailing instances`
- apache2: `/etc/apache2/sites-enabled/{001-event.conf, 000-main.conf, 000-macros.conf}`
- bind9: 
  - `/etc/bind/named.conf.*` for client matching and zone forwarding 
  - `/etc/bind/zones/*` for the different nameserver zones
- isc-dhcp-server
  - default `chkconfig off`
  - only needed if you want the replica to operate as kind of router (e.g. turn off dhcp and dns from FritzBox)
  - prepared configs: `/etc/dhcp/dhcpd.conf`
    - 1x /22 network for cube (with static MAC/IP mappings for some clients)
    - 1x /24 network for SAP-lounge setup
- user: sailing => `/home/sailing/servers/{servers,test}`
- Maven: ready to build code under `/home/sailing/code/`
- autossh tunnel: started at replica startup, defined in /etc/rc.local
    - redirects ports 1234(ssh), 1235(gwt), 1236(apache) to sapsailing.com where you can redirect the tunnel to your local pc

### Hostnames
- the replica should be available as `sailing-demo.sapsailing.com` from every local network
- the replica should be available as `event.sapsailing.com` from the desired network where replica should operate instead of EC2 instance

### Set up replication with dns and apache

The server runs a bind9 DNS server which is set as the primary DNS for the meraki networks which should server the replica instead of the event EC2 instance. 

Other then the event EC2 instance DNS requests are forwarded to a public DNS server (8.8.8.8). For the event server which should be locally replaced by the replica, e.g. 505worlds2014.sapsailing.com, the local DNS server returns the IP address of the cube server for the NIC through which the DNS request was received (this is received through client-matching). 

For that three DNS zones are introduced, which match by the incoming IP range request (choose what you need here, depending on the local setup!). See `/etc/bind/named.conf.zones-cube`, `/etc/bind/named.conf.zones-launch` and `/etc/bind/named.conf.zones-admin` for definition of zones and forwarding.

Take a look at `/etc/bind/zones/kwsapsailing.cube.db` and `/etc/bind/zones/kwsapsailing.admin.db` for the nameserver entries for the zone. The result is, that if a request comes from the lounge network, the domain is resolved to the ip the cube server has in this subnet and also if the request comes from the cube, it returns the ip in the subnet of the cube.

It depends on the network setup if you need this client matching or not. The Meraki supports inter-VLAN Routing, which leads to no need to multiple IP adresses for the replica server. Instead put the replica server into one of the networks and then restrict access through firewall rules. Furthermore just setup a single dns zone (e.g. `/etc/bind/named.conf.zones-cube` in which everything is maintained.

As described in bug2204 wind data and API are not served through the replica, so BeTomorrow has to go to our main server. For that I introduced a subsubdomain `btmw.event.sapsailing.com` which points to `sapsailing.com` and answers with our main active server of the event. This has to be clarified if we need this or if the replica operates correctly and servers everything as RESTlets..

On the cube server we run a replica server instance as user `sailing` under the usual directory set-up (`/home/sailing/servers/server`). The `env.sh` file uses a configuration that lets the server start replicating when launched. The two possible master servers (A / B) are specified in two lines defining `REPLICATE_MASTER_SERVLET_HOST`, one is commented, the other one active.

I have also prepared a second sailing instance as user `sailing` under the usual directory set-up (`/home/sailing/servers/test`) which could also be used to switch between to local instances if needed.

When switching from the A server to the B server or vice versa, the replica needs to be re-configured. Before stopping it, connected clients need to be re-directed to the master when they hit `event.sapsailing.com`. This is achieved by re-configuring the Apache server running on the replica, as user `root`, editing `/etc/apache2/sites-enabled/001-events.conf` and activating the line for `event.sapsailing.com` that points to the IP address of the new active server, then invoking `/etc/init.d/apache2 reload`. Once this is done, the running replica should cleanly stop replication (using the `Replication` tab in the AdminConsole), then stopping the instance (as user `sailing` run `/home/sailing/servers/server/stop`). Then, in `env.sh` the `REPLICATE_MASTER_SERVLET_HOST` variable needs to be set to point to the new master. After that, the replica Java instance can be launched using `/home/sailing/servers/server/start`). Before re-activating it is important that the rabbit initial load is done. So please watch either `/home/sailing/servers/server/logs/sailing0.log.0` or the Rabbit Webinterface on `http://RABBIT-MQ-SERVER:15672/#/queues`. 
Please deleted eventually displayed old queues (be sure not to remove the correct replicating queue!). Re-configure Apache again by editing `/etc/apache2/sites-enabled/001-events.conf` again, re-activating the record that lets event.sapsailing.com point to the replica. From this point on, clients in the Lounge, Cube and eventual other networks will instantly be directed (without connection loss!) to the cube server again when hitting the replica server's Apache with a URL starting with `event.sapsailing.com`. 

### Monitor the server
- You can check the load with `htop`
- You can check the accesses to apache with `apachetop` or you go to `http://sailing-demo.sapsailing.com/server-status`page to display the http status
- You can check the system temperature (depending on local environment may be important!) with `sensors` command

### Switching Servers

For maximum availability we normally have at least two EC2 servers reserved for a bigger event (e.g. building new releases in parallel and then switching, or with ELB). We can prepare the other server by installing a release, either by using the `refreshInstance.sh` script or by using `tmux attach` to go to a console and fetching from git and building and installing there. When done, fire up the server and load all races required. Test the server reasonably well.

Then switch the cube server replica to point to the new server. This process is described above.

Then, log on as `root` on `sapsailing.com`, go to `/etc/httpd/conf.d` and edit `001-events.conf` to update the event-related URLs. By convention, the inactive server URLs are suffixed with `-a` or `-b` name, respectively. When done editing, run `/etc/init.d/httpd reload` which will update the Apache configuration so that from that point on requests to `event.sapsailing.com` will go to the new server. Convince yourself by looking at the logs under `/home/sailing/servers/server/logs/sailing0.log.0` that the correct server is now active. Afterwards, stop the old server's Java instance.

### Local builds on replica
- verify on which branch you are inside `/home/sailing/code/` with `git status`
- do a `git fetch` / `git pull`
- eventually switch branches for a new build with `git co branch`
- eventually merge other stuff to that branch `git merge origin/branch`
- start doing a maven build with `./configuration/buildAndUpdateProduct.sh -t build`
- after build is **SUCCESSFULLY FINISHED** install  build with `./configuration/buildAndUpdateProduct.sh -s server install`
- now you are able to stop and start sailing instance (be careful with correct replication handling -> see above)