# Creating an Amazon AWS EC2 Image from Scratch

I started out with a clean "Amazon Linux AMI 2015.03 (HVM), SSD Volume Type - ami-a10897d6" image from Amazon and added the existing Swap and Home snapshots as new volumes. The root/system volume I left as is, to start with. This requires having access to a user key that can be selected when launching the image.

Enable the EPEL repository by issuing `yum-config-manager --enable epel/x86_64`.		

I then did a `yum update` and added the following packages:

 - httpd
 - mod_proxy_html
 - tmux
 - nfs-utils
 - chrony
 - libstdc++48.i686 (for Android builds)
 - glibc.i686 (for Android builds)
 - libzip.i686 (for Android builds)
 - telnet
 - apachetop
 - goaccess
 - postfix (for sending e-mail, e.g., to invite competitors and buoy pingers)
 - tigervnc-server
 - WindowMaker
 - xterm
 - sendmail-cf

In order to be able to connect to AWS DocumentDB instances, the corresponding certificate must be installed into the JVM's certificate store:

```
   wget -O /tmp/rds.pem https://s3.amazonaws.com/rds-downloads/rds-combined-ca-bundle.pem
   /opt/sapjvm_8/bin/keytool -importcert -alias AWSRDS -file /tmp/rds.pem -keystore /opt/sapjvm_8/jre/lib/security/cacerts -noprompt -storepass changeit
```

A latest MongoDB shell is installed by the following:

```
cat << EOF >/etc/yum.repos.d/mongodb-org.3.6.repo
[mongodb-org-3.6]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/amazon/2013.03/mongodb-org/3.6/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://www.mongodb.org/static/pgp/server-3.6.asc
EOF

yum update
yum install mongodb-org-shell
```

Then I created a mount point /home/sailing and copied the following lines from the /etc/fstab file from an existing SL instance:

```
UUID=a1d96e53-233f-4e44-b865-c78b862df3b8       /home/sailing   ext4    defaults,noatime,commit=30      0 0
UUID=7d7e68a3-27a1-49ef-908f-a6ebadcc55bb       none    swap    sw      0       0

# Mount the Android SDK from the Build/Dev box; use a timeout of 10s (100ds)
172.31.28.17:/home/hudson/android-sdk-linux     /opt/android-sdk-linux  nfs     tcp,intr,timeo=100,retry=0
172.31.18.15:/var/log/old       /var/log/old    nfs     tcp,intr,timeo=100,retry=0
```

This will mount the swap space partition as well as the /home/sailing partition, /var/log/old and the Android SDK stuff required for local builds.

In `/etc/ssh/sshd_config` I commented the line

```
# Only allow root to run commands over ssh, no shell
#PermitRootLogin forced-commands-only
```

and added the lines

```
PermitRootLogin without-password
PermitRootLogin Yes
```

to allow root shell login.

I copied the JDK7/JDK8 installations, particularly the current sapjvm_8 VM, from an existing SL instance to /opt.

I linked /etc/init.d/sailing to /home/sailing/code/configuration/sailing and added the following links to it:

```
rc0.d/K10sailing
rc1.d/K10sailing
rc2.d/S95sailing
rc3.d/S95sailing
rc4.d/S95sailing
rc5.d/S95sailing
rc6.d/K10sailing
```

Linked /etc/profile.d/sailing.sh to /home/sailing/code/configuration/sailing.sh. As this contains a PATH entry for /opt/amazon and the new image has the Amazon scripts at /opt/aws, I aldo created a symbolic link from /opt/amazon to /opt/aws to let this same path configuration find those scripts under the old and the new images.

Added the lines

```
# number of connections the firewall can track
net.ipv4.ip_conntrac_max = 131072
```

to `/etc/sysctl.conf` in order to increase the number of connections that are possible concurrently.

Added the following two lines to `/etc/security/limits.conf`:

```
*               hard    nproc           unlimited
*               hard    nofile          65000
```

This increases the maximum number of open files allowed from the default 1024 to a more appropriate 65k.

Copied the httpd configuration files `/etc/httpd/conf/httpd.conf`, `/etc/httpd/conf.d/000-macros.conf` and the skeletal `/etc/httpd/conf.d/001-events.conf` from an existing server. Make sure the following lines are in httpd.conf:

<pre>
  SetEnvIf X-Forwarded-For "^([0-9]*\.[0-9]*\.[0-9]*\.[0-9]*).*$" original_client_ip=$1
  LogFormat "%v %h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" combined
  LogFormat "%v %{original_client_ip}e %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" first_forwarded_for_ip
  CustomLog logs/access_log combined env=!original_client_ip
  CustomLog logs/access_log first_forwarded_for_ip env=original_client_ip
</pre>

They ensure that the original client IPs are logged also if the Apache server runs behind a reverse proxy or an ELB. See also [the section on log file analysis](/wiki/howto/development/log-file-analysis#log-file-analysis_log-file-types_apache-log-files).

Copied /etc/logrotate.conf from an existing SL instance so that `/var/log/logrotate-target` is used to rotate logs to.

Instead of having the `ANDROID_HOME` environment variable be set in `/etc/profile` as in the old instances, I moved this statement to the `sailing.sh` script in git at `configuration/sailing.sh` and linked to by `/etc/profile.d/sailing.sh`. For old instances this will set the variable redundantly, as they also have it set by a manually adjusted `/etc/profile`, but this shouldn't hurt.

Had to fiddle a little with the JDK being used. The default installation has an OpenJDK installed, and the AWS tools depend on it. Therefore, it cannot just be removed. As a result, it's important that `env.sh` has the correct `JAVA_HOME` set (/opt/jdk1.8.0_45, in this case). Otherwise, the OSGi environment won't properly start up.

To ensure that chronyd is started during the boot sequence, issued the command

```
chkconfig chrony on
```

which creates the necessary entries in the rc*.d directories.

Update the file `/etc/postfix/main.cf` in order to set the server's sending hostname to `sapsailing.com` as follows:
```
      myhostname = sapsailing.com
```

Adjust the /etc/sysconfig/vncservers settings to something like:

```
VNCSERVERS="2:sailing"
VNCSERVERARGS[2]="-geometry 1600x900"
```

## Mail Relaying
For setting up mail relaying towards central postfix server, have a look [here](https://wiki.sapsailing.com/wiki/info/landscape/mail-relaying)