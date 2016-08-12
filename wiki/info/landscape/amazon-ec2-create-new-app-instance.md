# Create a new Amazon app instance

The following screenshots will help you creating a new Amazon application instance. An application instance is the container for exactly one Java application server. Each instance is configured using so called "user data". This configuration will then be read by the java server.

## AMI Selection
In the "Classic Wizard" select "My AMIs" and then select "Sailing Analytics Java Server"

<img src="/wiki/images/amazon/StartInstance1.JPG" width="70%" height="70%"/>

Then select an instance type that matches what you want - be aware of the fact that t1.micro is not suitable for build process.

<img src="/wiki/images/amazon/StartInstance2.JPG" width="70%" height="70%"/>

Select a subnet that matches the one of the database server (in most cases this will be eu-west-1c). Leave other options untouched but scroll down to get to the "Advanced Options". There fill the user data field.

<img src="/wiki/images/amazon/StartInstance3.JPG" width="70%" height="70%"/>

Leave Kernel and RAM disk to Default. Then put the configuration for this server into the text area labeled "User Data" (Advanced Data). Here one example that loads a specific release package.

<img src="/wiki/images/amazon/UserDataDetails.JPG" width="70%" height="70%"/>

<pre>
INSTALL_FROM_RELEASE=name-of-release
USE_ENVIRONMENT=live-server
MONGODB_PORT=10202
MONGODB_NAME=myspecificevent
REPLICATION_CHANNEL=myspecificevent
SERVER_NAME=MYSPECIFICEVENT
BUILD_COMPLETE_NOTIFY=youe.name@sap.com
SERVER_STARTUP_NOTIFY=your.name@sap.com
</pre>

Provide your instance with an useful name. Usually, an SAP Sailing Analytics server instance should have a name that starts with "SL".

<img src="/wiki/images/amazon/StartInstance5.JPG" width="70%" height="70%"/>

Make absolutely sure to select the right Security Group. If you do not select the group "Sailing Analytics App" your server won't be able to access any database.

<img src="/wiki/images/amazon/StartInstance6.JPG" width="70%" height="70%"/>

Configure the key. Make sure to always use the "Administrator" key.

<img src="/wiki/images/amazon/StartInstance7.JPG" width="70%" height="70%"/>

Your instance is now launching. You can create alarms if you want to monitor the state of the application. Be aware that monitoring is costly.

<img src="/wiki/images/amazon/StartInstance12.JPG" width="70%" height="70%"/>

Should the reachability status check fail, your instance will not be reachable, and the best option is to terminate and re-create that instance. Sorry.

Your application is now ready. Check that everything is green.

<img src="/wiki/images/amazon/StartInstance13.JPG" width="70%" height="70%"/>

You can access the application server by using the DNS and the port you've configured.

<img src="/wiki/images/amazon/StartInstance14.JPG" width="70%" height="70%"/>
