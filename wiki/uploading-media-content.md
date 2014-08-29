# Uploading Media Content

We have a place to store media content such as images and videos, e.g., created at sailing events. This place is the directory `/var/www/static` on our web server, and it is publicly reachable by HTTP at `http://static.sapsailing.com/...`. For example, the file `/var/www/static/images/splash-screen-compressed-02.png` is reachable via the URL `http://static.sapsailing.com/images/splash-screen-compressed-02.png`.

Uploading content to this directory works best using the "secure copy" (SCP) protocol, sometimes also referred to as SFTP. It is basically sending files through a secure shell (SSH) connection. Command line aficionados will know the "scp" command and its options and will understand what it means to have a public/private key pair that allows them to log on to the `sapsailing.com` web server.

For those who don't, we can easily create username/password accounts on the web server which spares the technical tidbits of public/private key pair creation and registration. A good graphical tool (for the less command line-oriented folks) is WinSCP which can be downloaded at [http://winscp.net](http://winscp.net/eng/download.php#download2).

For those in an open Internet environment (outside the SAP VPN or logged on to the "SAP Internet" WiFi) the hostname to use is `sapsailing.com` and the port number is `22`. Inside the SAP VPN we have to use a little trick to make the connection. Use `10.18.22.50` as the hostname and `12349` as the port number.