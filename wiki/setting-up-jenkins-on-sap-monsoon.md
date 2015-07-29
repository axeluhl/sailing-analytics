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