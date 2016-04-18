# Setup central mail server (instance "Webserver")
- you need to add the local subnet (which the instance which should relay) is in
  - edit `/etc/postfix/main.cf` and edit `mynetworks = x.y.z.w/v` to match the subnet. Currently all subnets from VPC Ireland and US East are in here, this means adding an instance to existing subnets / zones will not require a change here
  - restart postfix with `service postfix restart`
  - check the log if everything is working with `tailf /var/log/maillog`
# Setup standalone instance for relaying
- on each Sailing App instance a local sendmail client will be running, which is in default using its own binaries to send out mails directly
- the problem here is, that external mail servers will check actually the domain "sapsailing.com" and its MX and A-Record. IF this does not match (it does not, because A/MX goes to central webserver) most correctly mailservers will deferr or even reject the incoming mail as it looks like hard spam
- to prevent this you need to relay mails send from any instance to the central postfix (with the correct A and MX records), so that other mail servers dont reject mails
  - install package `sendmail-cf` via `yum install`
  - edit the config `/etc/mail/sendmail.mc` and insert the line 
```
define(`SMART_HOST', `ip-172-31-18-15.eu-west-1.compute.internal')dnl
``` and save
  - regenerate the sendmail config with `. /etc/mail/make` 
  - restart sendmail with `service sendmail restart`
- test the setup by tailing both `/var/log/maillog`'s and send a mail from the relayed instance via `echo "test" | sendmail yourmail@domain.tld`

=> all this shall be included to a next HVM version 