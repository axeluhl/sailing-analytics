# SAP Sailing Analytics
The SAP Sailing Analytics platform as seen on [sapsailing.com](https://sapsailing.com)

Sailing provides the perfect platform for SAP to showcase solutions and help the sport run like never before. SAP’s involvement in the sport has transformed the sailing experience by providing tools, which:

Help sailors analyze performance and optimize strategy
Bring fans closer to the action
Provide the media with information and insights to deliver a greater informed commentary
SAP has a longstanding involvement with sailing and has established a portfolio spanning across teams and regattas.

Start contributing: [https://wiki.sapsailing.com/wiki/howto/onboarding](https://wiki.sapsailing.com/wiki/howto/onboarding)

## Building and Running

To build, invoke
```
    configuration/buildAndUpdateProduct.sh build
```
If the build was successful you can install the product locally by invoking
```
    configuration/buildAndUpdateProduct.sh install [ -s <server-name> ]
```
The default server name is taken to be your current branch name, e.g., ``master``. The install goes to ``${HOME}/servers/{server-name}``. You will find a ``start`` script there which you can use to launch the product.

## Downloading, Installing and Running an Official Release

You need to have Java 8 installed. Get one from [here](https://tools.eu1.hana.ondemand.com/#cloud). Either ensure that this JVM's ``java`` executable in on the ``PATH`` or set ``JAVA_HOME`` appropriately.

At [https://releases.sapsailing.com](https://releases.sapsailing.com) you find official product builds. To fetch and install one of them, make an empty directory, change into it and run the ``refreshInstance.sh`` command, e.g., like this:
```
    mkdir sailinganalytics
    cd sailinganalytics
    echo "MONGODB_URI=mongodb://localhost/winddb" | ${GIT_ROOT}/java/target/refreshInstance.sh auto-install-from-stdin
```
This will download and install the latest release and configure it such that it will connect to a MongoDB server running locally (``localhost``) and listening on the default port ``27017``, using the database called ``winddb``.

Launch the server from the directory to which you just installed it:
```
    ./start
```
See the server logs like this:
```
    tail -f logs/sailing0.log.0
```
Connect to your server at ``http://localhost:8888`` and find its administration console at ``http://localhost:8888/gwt/AdminConsole.html``. The first-time default login user is ``admin`` with default password ``admin`` (please change).

## Docker

To build a docker image, try ``docker/makeImageForLatestRelease``. The upload to the default (private) Dockerhub repository will usually fail unless you are a collaborator for that repository, but you should see a local image tagged ``docker.sapsailing.com:443/sapsailing:...`` result from the build. To run that docker image, try something like
```
    docker run -d -e "MEMORY=4g" -e "MONGODB_URI=mongodb://my.mongohost.org?replicaSet=rs0&retryWrites=true" -P <yourimage>
```
Do a "docker ps" to figure out the port exposing the web application:

CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAMES 79f6faf19b6a docker.sapsailing.com:443/sapsailing:latest "/home/sailing/serve…" 33 seconds ago Up 32 seconds 0.0.0.0:32782->6666/tcp, 0.0.0.0:32781->7091/tcp, 0.0.0.0:32780->8000/tcp, 0.0.0.0:32779->8888/tcp, 0.0.0.0:32778->14888/tcp modest_dhawan

In this example, find your web application at http://localhost:32779 which is where the port 8888 exposed by the application is exposed at on your host. In the example with telnet port 14888 mapped to localhost:32788 do a
```
    telnet localhost 32778
```
to connect to the server's OSGi console.

## Docker Compose

If you have built or obtained the ``docker.sapsailing.com:443/sapsailing:latest`` image, try this:
```
    cd docker
    docker-compose up
```
Based on the ``docker/docker-compose.yml`` definition you should end up with three running Docker containers:
- a MongoDB server, listening on default port 27017
- a RabbitMQ server, listening on default port
- a Sailing Analytics server, listening for HTTP requests on port 8888 and for telnet connections to the OSGi console on port 14888

Try a request to [``http://127.0.0.1:8888/index.html``](http://127.0.0.1:8888/index.html) or [``http://127.0.0.1:8888/gwt/status``](http://127.0.0.1:8888/gwt/status) to see if things worked.

## Configuration Options, Environment Variables

The server process can be configured in various ways. The corresponding environment variables you may use during installation with ``refreshInstance.sh`` and for setting up your Docker environment can be found in the following files:
- [DefaultProcessConfigurationVariables.java](java/com.sap.sse.landscape/src/com/sap/sse/landscape/DefaultProcessConfigurationVariables.java)
- [SailingProcessConfigurationVariables.java](java/com.sap.sailing.landscape/src/com/sap/sailing/landscape/procedures/SailingProcessConfigurationVariables.java)
