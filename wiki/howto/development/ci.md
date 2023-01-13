# Continuous Integration with Hudson/Jenkins

Our default Hudson runs on https://hudson.sapsailing.com. If you need access, please contact axel.uhl@sap.com or simon.marcel.pamies@sap.com. We have a build job running for the master branch which will automatically pick up any changes, run a build with tests and inform committers about flaws they introduced that broke the build.

It is good practice to set up a new Hudson job for major branches that require solid testing before being merged into the master branch. The entry page at http://hudson.sapsailing.com explains how to do this. It basically comes down to copying a template job and adjusting the branch name. As easy as that :-)

## Collecting measurements using Hudson/Jenkins

If you have a test case that measures something, such as performance or level of fulfillment or any other numeric measure, you can have Hudson/Jenkins plot it. In your test case, use the class `com.sap.sailing.domain.test.measurements.MeasurementXMLFile` and add performance cases to which you add measurements, e.g., as follows:
<pre>
        MeasurementXMLFile performanceReport = new MeasurementXMLFile(getClass());
        MeasurementCase performanceReportCase = performanceReport.addCase(getClass().getSimpleName());
        performanceReportCase.addMeasurement(new Measurement("My Measurement", theNumberIMeasured));
        performanceReport.write();
</pre>

## In case you'd like to set up your own Hudson/Jenkins

Initially we had trouble with Jenkins and the GIT plug-in. However, https://issues.jenkins-ci.org/browse/JENKINS-13381?focusedCommentId=196689&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-196689 explains that installing the Credentials plugin into Jenkins may help. Basically, what is needed over and above a plain Jenkins installation are the following plug-ins:

* Credentials
* Jenkins GIT
* Jenkins GIT Client
* Measurement Plots
* SSH Credentials
* Xvnc

Note, though that the Xvnc plug-in in version 1.16 seems to be causing some trouble (https://issues.jenkins-ci.org/browse/JENKINS-22105). Downgrading to 1.14 helps. The 1.14 .hpi file can be obtained, e.g., here: http://www.filewatcher.com/m/xvnc.hpi.21882-0.html.

Make sure that the environment used to run Hudson/Jenkins uses a UTF-8 locale. Under Linux, it's a good idea to set the environment variables
<pre>
    export LC_ALL=en_US.UTF-8
    export LANG=us_US.UTF-8
</pre>
which can, depending on how your Hudson/Jenkins is started, be included, e.g., in `/etc/init/jenkins` which then should have a section that looks like this:
<pre>
script
    [ -r /etc/default/jenkins ] && . /etc/default/jenkins
    export JENKINS_HOME
    export LC_ALL=en_US.UTF-8
    export LANG=us_US.UTF-8
    exec start-stop-daemon --start -c $JENKINS_USER --exec $JAVA --name jenkins \
        -- $JAVA_ARGS -jar $JENKINS_WAR $JENKINS_ARGS --logfile=$JENKINS_LOG
end script
</pre>
Other options for setting the locale include adding the LC_ALL and LANG variables to the `/etc/environment` file.

The basic idea of setting up a build job is to create a so-called "free-style software project" which then executes our `configuration/buildAndUpdateProduct.sh` script using the `build` parameter. Top-down, the following adjustments to a default free-style job that are required for a successful build are these:

* select "Git"
* enter `ssh://trac@sapsailing.com/home/trac/git` as the Repository URL
* create credentials using the `Add` button, e.g., pasting your private key and providing Jenkins with the password
* enter `master` for "Branches to build"
* under "Build Triggers" check "Poll SCM" and enter `H/1 * * * *` for the schedule which will check for updates in git every minute
* under "Build Environment" check "Run Xvnc during build"
* under "Build" select "Add build step" --> "Execute Shell" and paste as command something like this: `ANDROID_HOME=/usr/local/android-sdk-linux configuration/buildAndUpdateProduct.sh build`. Adjust the location of the Android SDK accordingly and install it if not already present.
* as Post-build Action, select "Publish JUnit test result report" and as Test report XMLs provide `**/TEST-*.xml` as the file pattern for the test reports.
* check the "Additional test reports features / Measurement Plots" box
* provide e-mail notification settings as you see fit

## Hudson Master/Slave Set-Up

In order to elastically scale our build / CI infrastructure, we use AWS to provide Hudson build slaves on demand. The Hudson Master (https://hudson.sapsailing.com) has a script obtained from our git at ``./configuration/launchhudsonslave`` which takes an Amazon Machine Image (AMI), launches it in our default region (eu-west-1) and connects to it. The AWS credentials are stored in the ``root`` account on ``hudson.sapsailing.com``, and the ``hudson`` user is granted access to the script by means of an ``/etc/sudoers.d`` entry.

The image has been crafted specifically to contain the tools required for the build. In order to set up such an image based on Ubuntu, consider running the following commands as root on a fresh Ubuntu 20.04 instance with a 100GB root partition, starting as the "ubuntu" user:

```
   scp -o StrictHostKeyChecking=false trac@sapsailing.com:/home/wiki/gitwiki/configuration/imageupgrade_functions.sh /tmp
   scp -o StrictHostKeyChecking=false trac@sapsailing.com:/home/wiki/gitwiki/configuration/hudson_slave_setup/* /tmp
   sudo -i
   # For Ubuntu 22.x install libssl1.1:
   wget http://archive.ubuntu.com/ubuntu/pool/main/o/openssl/libssl1.1_1.1.1f-1ubuntu2_amd64.deb
   dpkg -i libssl1.1_1.1.1f-1ubuntu2_amd64.deb
   rm libssl1.1_1.1.1f-1ubuntu2_amd64.deb
   # For all O/S versions:
   dd if=/dev/zero of=/var/cache/swapfile bs=1G count=20
   chmod 600 /var/cache/swapfile
   mkswap /var/cache/swapfile
   echo "/var/cache/swapfile none swap sw 0 0" >>/etc/fstab
   swapon -a
   mkdir /opt/android-sdk-linux
   echo "dev.internal.sapsailing.com:/home/hudson/android-sdk-linux /opt/android-sdk-linux nfs tcp,intr,timeo=100,retry=0" >>/etc/fstab
   apt-get update
   apt-get install -y unzip xvfb libxi6 libgconf-2-4 nfs-common gnupg
   curl -sS -o - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add
   wget -qO - https://www.mongodb.org/static/pgp/server-4.4.asc | sudo apt-key add -
   echo "deb https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/4.4 multiverse" >/etc/apt/sources.list.d/mongodb-org-4.4.list
   apt-get -y update
   apt-get -y upgrade
   # The following work well on Ubuntu 20.04 but not on Ubuntu 22.04 or Debian 11 because
   # the firefox package may be "firefox-esr" and firefox-geckodriver and the linux*aws packages may not be available at
   # all, in which case a direct download, e.g., from https://github.com/mozilla/geckodriver/releases/download/v0.31.0/geckodriver-v0.31.0-linux64.tar.gz
   # is an alternative; unpack to /usr/local/bin.
   cd /usr/local/bin
   wget -O - "https://github.com/mozilla/geckodriver/releases/download/v0.31.0/geckodriver-v0.31.0-linux64.tar.gz" | tar xzvpf -
   apt-get -y install cloud-guest-utils maven rabbitmq-server mongodb-org firefox fwupd linux-aws linux-headers-aws linux-image-aws docker.io
   apt-get -y autoremove
   cd /tmp
   mv /tmp/imageupgrade /usr/local/bin
   mv /tmp/imageupgrade_functions.sh /usr/local/bin
   mv /tmp/mounthudsonworkspace /usr/local/bin
   mv /tmp/*.service /etc/systemd/system/
   source /usr/local/bin/imageupgrade_functions.sh
   download_and_install_latest_sap_jvm_8
   systemctl daemon-reload
   systemctl enable imageupgrade.service
   systemctl enable mounthudsonworkspace.service
   systemctl enable mongod.service
   systemctl enable rabbitmq-server.service
   adduser --system --shell /bin/bash --quiet --group --disabled-password sailing
   adduser --system --shell /bin/bash --quiet --group --disabled-password hudson
   adduser hudson docker
   # Now log in to the docker registry at docker.sapsailing.com:443 with a valid user account for local user "hudson"
   sudo -u hudson docker login docker.sapsailing.com:443
   sudo -u sailing mkdir /home/sailing/.ssh
   sudo -u sailing chmod 700 /home/sailing/.ssh
   sudo -u hudson mkdir /home/hudson/.ssh
   sudo -u hudson chmod 700 /home/hudson/.ssh
   sudo -u hudson mkdir /home/hudson/workspace
   sudo -u hudson git config --global user.email "hudson@sapsailing.com"
   sudo -u hudson git config --global user.name "Hudson CI SAP Sailing Analytics"
   # Now add a password-less private ssh key "id_rsa" to /home/sailing/.ssh and make sure it is eligible to access trac@sapsailing.com
   chown sailing /home/sailing/.ssh/id_*
   chgrp sailing /home/sailing/.ssh/id_*
   chmod 600 /home/sailing/.ssh/id_*
   cp /home/sailing/.ssh/id_* /home/hudson/.ssh
   chown hudson /home/hudson/.ssh/*
   chgrp hudson /home/hudson/.ssh/*
   chmod 600 /home/hudson/.ssh/id_*
   # ensure the host key of sapsailing.com is accepted:
   sudo -u sailing ssh -o StrictHostKeyChecking=false trac@sapsailing.com ls >/dev/null
   sudo -u sailing git clone trac@sapsailing.com:/home/trac/git /home/sailing/code
   sudo -u hudson mkdir /home/hudson/.m2
   chmod a+r /home/sailing
   chmod a+x /home/sailing
   sudo -u hudson cp /home/sailing/code/toolchains.xml /home/hudson/.m2
   sudo -u hudson ssh -o StrictHostKeyChecking=false trac@sapsailing.com ls >/dev/null
   echo "export JAVA_HOME=/opt/sapjvm_8" >/etc/profile.d/sapjvm.sh
   chmod a+x /etc/profile.d/sapjvm.sh
   echo "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6TjveiR+KkEbQQcAEcme6PCUHZLLU5ENRCXnXKaFolWrBj77xEMf3RrlLJ1TINepuwydHDtN5of0D1kjykAIlgZPeMYf9zq3mx0dQk/B2IEFSW8Mbj74mYDpQoUULwosSmWz3yAhfLRgE83C7Wvdb0ToBGVHeHba2IFsupnxU6gcInz8SfX3lP78mh4KzVkNmQdXkfEC2Qe/HUeDLdI8gqVtAOd0NKY8yv/LUf4JX8wlZb6rU9Y4nWDGbgcv/k8h67xYRI4YbtEDVkPBqCZux66JuwKF4uZ2q+rPZTYRYJWT8/0x1jz5W5DQtuDVITT1jb1YsriegOZgp9LfS11B7w== hudson@ip-172-31-28-17" >/home/hudson/.ssh/authorized_keys
   sudo -u hudson wget -O /home/hudson/slave.jar "https://hudson.sapsailing.com/jnlpJars/slave.jar"
```

Furthermore, the ephemeral storage is partitioned with a ``gpt`` label into a swap partition with 8GB and the remainder as an ``ext4`` partition mounted under ``/ephemeral/data`` with is then bound with a "bind" mount to ``/home/hudson/workspace``. See the ``/etc/systemd/system/mounthudsonworkspace.service`` systemd service definition on the slave instances. The ``launchhudsonslave`` script launches the instance, checks for it to enter the ``running`` state, then tries to connect using SSH with user ``hudson``. The respective keys are baked into the image and match up with the key stored in ``hudson@hudson.sapsailing.com:.ssh``.

The ``launchhudsonslave`` script will then establish the SSH connection, launching the ``slave.jar`` connector. When the Hudson Master disconnects, the Java VM running ``slave.jar`` will terminate, and the next script command of ``launchhudsonslave`` will shutdown the host. This is possible for user ``hudson`` due to corresponding entries under ``/etc/sudoers.d``. The hosts are launched such that shutting them down will terminate the Amazon EC2 instance.

## Hudson patch for mail-1.6.2

With JDKs around 1.8.0_291 an original Hudson installation faces trouble when sending out e-mails through TLS-secured SMTP servers such as Amazon Simple Email Service (SES). The problem can be solved by replacing ``WEB-INF/lib/mail-1.4.4.jar`` in the ``/usr/lib/hudson/hudson.war`` file by a newer copy, such as ``mail-1.6.2.jar``, sometimes also referred to as ``com.sun.mail-1.6.2.jar`` or ``javax.mail-1.6.2.jar``. A correspondingly patched version can be found at [https://static.sapsailing.com/hudson.war.patched-with-mail-1.6.2](https://static.sapsailing.com/hudson.war.patched-with-mail-1.6.2).
