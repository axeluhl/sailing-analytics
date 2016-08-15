# Uploading Media Content

We have a place to store media content such as images and videos, e.g., created at sailing events. This place is the directory `/var/www/static` on our web server, and it is publicly reachable by HTTP at `http://static.sapsailing.com/...`. For example, the file `/var/www/static/images/splash-screen-compressed-02.png` is reachable via the URL `http://static.sapsailing.com/images/splash-screen-compressed-02.png`.

Uploading content to this directory works best using the "secure copy" (SCP) protocol, sometimes also referred to as SFTP. It is basically sending files through a secure shell (SSH) connection. Command line aficionados will know the "scp" command and its options and will understand what it means to have a public/private key pair that allows them to log on to the `sapsailing.com` web server.

For those who don't, we can easily create username/password accounts on the web server which spares the technical tidbits of public/private key pair creation and registration. A good graphical tool (for the less command line-oriented folks) is WinSCP which can be downloaded at [http://winscp.net](http://winscp.net/eng/download.php#download2).

For those in an open Internet environment (outside the SAP VPN or logged on to the "SAP Internet" WiFi) the hostname to use is `sapsailing.com` and the port number is `22`. Inside the SAP VPN we have to use a little trick to make the connection. Use `10.18.22.50` as the hostname and `12349` as the port number.

## Quick Introduction to the Use of WinSCP

After downloading WinSCP from the URL shown above, run the installer. When it asks you about your preferred user interface style you may want to choose "Explorer" which may be most familiar for Windows users.

![WinSCP Install](/wiki/images/winscp/winscp-install-1.png)

Then, launch WinSCP. It welcomes you with a login screen:

![WinSCP Login](/wiki/images/winscp/winscp-login.png)

Enter the hostname and port number as described above, depending on your network environment (within SAP network or outside of SAP network). Add your user name and password, then click the "Login" button. You should then see an Explorer-like window that shows the web server's file system hierarchy.

Navigate to /var/www/static:

![WinSCP /var/www/static](/wiki/images/winscp/winscp-var-www-static.png)

You can create new folders using the "Create Folder" button from the toolbar:

![WinSCP Create Folder](/wiki/images/winscp/winscp-create-folder.png)

Files can be uploaded from your local disk by dragging and dropping them from a local Windows Explorer running on your desktop onto the content panel in the right part of the WinSCP window.