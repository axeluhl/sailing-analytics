# Creating an Amazon AWS EC2 Image from Scratch

I started out with a clean "Amazon Linux AMI 2015.03 (HVM), SSD Volume Type - ami-a10897d6" image from Amazon and added the existing Swap and Home snapshots as new volumes. The root/system volume I left as is, to start with. This requires having access to a user key that can be selected when launching the image.

I then did a `yum update` and added the following packages:

 - httpd
 - mod_proxy_html
 - tmux

Then I created a mount point /home/sailing and copied the following lines from the /etc/fstab file from an existing SL instance:

```
UUID=a1d96e53-233f-4e44-b865-c78b862df3b8       /home/sailing   ext4    defaults,noatime,commit=30      0 0
UUID=7d7e68a3-27a1-49ef-908f-a6ebadcc55bb       none    swap    sw      0       0

# Mount the Android SDK from the Build/Dev box; use a timeout of 10s (100ds)
172.31.28.17:/home/hudson/android-sdk-linux     /opt/android-sdk-linux  nfs     tcp,intr,timeo=100,retry=0
172.31.18.15:/var/log/old       /var/log/old    nfs     tcp,intr,timeo=100,retry=0
```

This will mount the swap space partition as well as the /home/sailing partition, /var/log/old and the Android SDK stuff required for local builds.