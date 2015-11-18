# Log File Analysis

[__TOC__]

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

**However**, one key problem of these decentralized Apache log files is that the client IP address only identifies the load balancer instance, not the original client itself. With this, any type of analysis that tries to count the number of visitors will deliver skewed results. Counting the plain number of hits, though, is OK, as is the identification of the most-frequently used referrer URLs which can, e.g., help to identify popular leaderboards and races.

### Amazon EC2 Elastic Load Balancer (ELB) Logs



## Analysis Tools

### goaccess

### apachetop

### AWStats

### Custom Scripts