# Continuous Integration with Hudson/Jenkins

Our default Hudson runs on http://hudson.sapsailing.com. If you need access, please contact axel.uhl@sap.com or simon.marcel.pamies@sap.com. We have a build job running for the master branch which will automatically pick up any changes, run a build with tests and inform committers about flaws they introduced that broke the build.

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