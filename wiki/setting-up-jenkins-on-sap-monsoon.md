# Setting up Jenkins on SAP Monsoon

Follow the initial setup and install guide, replacing the "hudson" user by a "jenkins" user: https://wiki.wdf.sap.corp/wiki/display/LeanDI/Jenkins+Setup+on+Monsoon+Ubuntu+Image

As mail host, use mail.sap.corp.

I added a jenkins_sapsailing user to our internal git repository at ``ssh://git.wdf.sap.corp:29418/SAPSail/sapsailingcapture.git``, generated an ssh-key for user ``jenkins`` using the ``ssh-keygen`` command and added the following to ~jenkins/.ssh/config:

<pre>
  Host git.wdf.sap.corp
    User jenkins_sapsailing
    HostName git.wdf.sap.corp
    IdentityFile ~/.ssh/id_rsa
</pre>

The following additional packages needed to install using apt-get install ...

 * libxrender1
 * libxtst6
 * libXi

The Monsoon instance on which I did this for the first time can currently (2015-07-29) be reached at http://mo-11705429d.mo.sap.corp:8080/jenkins

Add the following to the Maven configuration file at ``/home/jenkins/.m2/settings.xml``:

```
        <proxies>
           <proxy>
              <id>SAP-proxy</id>
              <active>true</active>
              <protocol>http</protocol>
              <host>proxy.wdf.sap.corp</host>
              <port>8080</port>
              <nonProxyHosts>*.sap.corp|nexus</nonProxyHosts>
            </proxy>
        </proxies>
```

## Git access

There is now HTTPS git access on https://git.sapsailing.com/git which maps to the Git repository at /home/trac/git. Valid user accounts for HTTP basic authentication are provided in `/etc/httpd/conf/passwd.git` which can be manipulated using the `htpasswd` tool. One default user `replicate` has been established to mirror the repository to the SAP-internal Git at `git.wdf.sap.corp`.