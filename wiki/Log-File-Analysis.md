# Log File Analysis

[[_TOC_]]

## Log File Types

There are three different types of log files produced by the server landscape under the umbrella of http://www.sapsailing.com:

 - Java server logs (sailing0.log.0, gc.log.0.current, ...)

 - Apache log files (access_log, error_log, ...)

 - Amazon EC2 Elastic Load Balancer (ELB) logs (017363970217_elasticloadbalancing_eu-west-1_ELBGermanLeague_20151118T1320Z_52.18.137.128_18dpodfi.log)

### Java Server Logs

The Java server logs contain health, memory and general progress data as well as information about exceptions that occurred, including deadlock situations and important application events such as adding a wind sensor account, starting to track a race, creating a new regatta, etc. The format of a typical Java server log line is

<pre>
Nov 03, 2015 6:32:24 PM com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl loadControlPointWithTwoMarks
INFO: Migrating right Mark of ControlPointWithTwoMarks Gate White P / White S from old field GATE_RIGHT to CONTROLPOINTWITHTWOMARKS_RIGHT
</pre>

A typical garbage collection log from gc.log.0.current may look like this:

<pre>
      ...
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.2 ms]
      [Free CSet: 2.2 ms]
   [Eden: 5406.0M(5406.0M)->0.0B(5398.0M) Survivors: 26.0M->32.0M Heap: 8156.2M(9216.0M)->2757.5M(9216.0M)]
 [Times: user=0.28 sys=0.01, real=0.19 secs]
</pre>

The `Heap: ...` section gives hints as to the current heap memory consumption before and after the last GC run as well as the maximum heap allocated so far.

### Apache Log Files

Our primary, central Apache web server largely acts as a reverse proxy that maps subdomain names to virtual host declarations by means of a configuration file located at `/etc/httpd/conf.d/001-events.conf` which uses all sorts of clever macros to keep these mapping definitions concise. Due to the large number of these virtual hosts and in order to be able to keep the logs apart, we use the "combined" log file format which lists the virtual host name used as the first column in the log entry. This lets an `access_log` entry look like this:

<pre>
dutchleague2015.sapsailing.com 94.209.86.140 - - [08/Nov/2015:10:47:31 +0000] "POST /gwt/service/sailing HTTP/1.1" 200 751 "http://dutchleague2015.sapsailing.com/gwt/RaceBoard.html&regattaName=Dutch%20Sailing%20League%202015%20-%20Almere" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.78.2 (KHTML, like Gecko) Version/6.1.6 Safari/537.78.2"
</pre>

After the virtual host name comes the client's IP address, followed by two unused fields regarding authentication, then the time stamp, the request, response code, number of bytes delivered, the referrer URL and the user agent.

For load-balanced set-ups we have an Amazon Elastic Load Balancer (ELB) dispatch incoming requests to the instances linked to the ELB. The ELB can (and definitely should) be configured to write its own logs (see next section), but those are not in Apache log file format and contain no information about the referrer URL. Using an Apache server on the instances linked to the ELB partly solves this problem by generating logs in the same format that our central Apache server uses.

In addition to this, the Apache servers on these load-balanced instances perform the same URL rewriting and reverse proxying using the same set of macros that the central Apache server uses.

**However**, one key problem of these decentralized Apache log files is that the client IP address only identifies the load balancer or reverse proxy instance, not the original client itself. With this, any type of analysis that tries to count the number of visitors will deliver skewed results. Counting the plain number of hits, though, is OK, as is the identification of the most-frequently used referrer URLs which can, e.g., help to identify popular leaderboards and races.

Still, in order to provide the true client IP in the log files, we use the `X-Forwarded-For` HTTP header field which contains the comma-separated list of "hops" through which the request was reverse-proxied so far. The following combination of Apache `httpd.conf` entries make this work:

<pre>
  SetEnvIf X-Forwarded-For "^([0-9]*\.[0-9]*\.[0-9]*\.[0-9]*).*$" original_client_ip=$1
  LogFormat "%v %h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" combined
  LogFormat "%v %{original_client_ip}e %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" first_forwarded_for_ip
  CustomLog logs/access_log combined env=!original_client_ip
  CustomLog logs/access_log first_forwarded_for_ip env=original_client_ip
</pre>

This uses the SetEnvIf module, tries to match an IP address at the start of the `X-Forwarded-For` header field, and if found, assigns it to the `original_client_ip` variable. This variable, in turn, decides which of the two `CustomLog` directives is applied to the request. If the variable is set, instead of using the regular `%h` field for the client IP, the variable value is logged by `%{original_client_ip}`.

### Amazon EC2 Elastic Load Balancer (ELB) Logs

An Amazon ELB can be configured to write log files to the S3 storage. The general format is [explained here](http://docs.aws.amazon.com/ElasticLoadBalancing/latest/DeveloperGuide/access-log-collection.html#access-log-entry-format). It contains in particular the client IP where the request originated and can tell timing parameters for request forwarding and processing that otherwise would not be available.

The format for these logs is

<pre>
timestamp elb client:port backend:port request_processing_time backend_processing_time response_processing_time elb_status_code backend_status_code received_bytes sent_bytes "request" "user_agent" ssl_cipher ssl_protocol
</pre>

It seems a good idea to always capture these logs for archiving purposes. They end up on an S3 bucket, and by default we use `sapsailing-access-logs`.

Content can be synced from there to a local directory using the following command:

``s3cmd sync s3://sapsailing-access-logs/elb-access-logs ./elb-access-logs/``

Our central web server at `www.sapsailing.com` does this periodically every day, sync'ing the logs to `/var/log/old/elb-access-logs/`.

## Automatic Log File Rotation to /var/log/old

When an EC2 server is launched from its Amazon Machine Image (AMI), the /etc/init.d/sailing script is executed with the "start" parameter. This script is really just a link to `/home/sailing/code/configuration/sailing` which comes from the git version checked out to the workspace at `/home/sailing/code`. This script patches the file `/etc/logrotate.d/httpd` such that when log rotation happens, the existence of the directory `/var/log/old/$SERVER_NAME/$SERVER_IP` is ensured and the log files are copied there.

This way, logs end up in a per server-name and per server-ip directory.

The general log rotation rules (size, time, compression, etc.) is governed by `/etc/logrotate.conf`.

## Analysis Tools

### goaccess

 - mention specific parameters and .goaccess config file required to parse our format

### apachetop

### AWStats

 - specific log collection approach configured in /etc/awstats/*.conf script

 - extension for per-virtual-host hit count

 - configuration of central apache to present results (awstats.sapsailing.com)

### Custom Scripts

 - unique_ips_per_referrer
 - convertELBLogToApacheFormat