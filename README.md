# SAP Sailing Analytics
The SAP Sailing Analytics platform as seen on [sapsailing.com](https://sapsailing.com)

Sailing provides the perfect platform for SAP to showcase solutions and help the sport run like never before. SAP’s involvement in the sport has transformed the sailing experience by providing tools, which:

Help sailors analyze performance and optimize strategy
Bring fans closer to the action
Provide the media with information and insights to deliver a greater informed commentary
SAP has a longstanding involvement with sailing and has established a portfolio spanning across teams and regattas.

Start contributing: [https://wiki.sapsailing.com/wiki/howto/onboarding](https://wiki.sapsailing.com/wiki/howto/onboarding)

To build, invoke
```
    configuration/buildAndUpdateProduct.sh build
```
If the build was successful you can install the product locally by invoking
```
    configuration/buildAndUpdateProduct.sh install [ -s <server-name> ]
```
The default server name is taken to be your current branch name, e.g., ``master``. The install goes to ``${HOME}/servers/{server-name}``. You will find a ``start`` script there which you can use to launch the product.

# Docker

To build a docker image, try ``docker/makeImageForLatestRelease``. The upload to the default (private) Dockerhub repository will usually fail unless you are a collaborator for that repository, but you should see a local image tagged ``donaldduck70/sapsailing:...`` result from the build. To run that docker image, try something like
```
    docker run -d -e "MEMORY=4g" -e "MONGODB_URI=mongodb://my.mongohost.org?replicaSet=rs0&retryWrites=true" -P <yourimage>
```
Do a "docker ps" to figure out the port exposing the web application:

CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAMES 79f6faf19b6a donaldduck70/sapsailing:latest "/home/sailing/serve…" 33 seconds ago Up 32 seconds 0.0.0.0:32782->6666/tcp, 0.0.0.0:32781->7091/tcp, 0.0.0.0:32780->8000/tcp, 0.0.0.0:32779->8888/tcp, 0.0.0.0:32778->14888/tcp modest_dhawan

In this example, find your web application at http://localhost:32779 which is where the port 8888 exposed by the application is exposed at on your host. In the example with telnet port 14888 mapped to localhost:32788 do a
```
    telnet localhost 32778
```
to connect to the server's OSGi console.

# Docker Compose

If you have built or obtained the ``donaldduck70/sapsailing:latest`` image, try this:
```
    cd docker
    docker-compose up
```
Based on the ``docker/docker-compose.yml`` definition you should end up with three running Docker containers:
- a MongoDB server, listening on default port 27017
- a RabbitMQ server, listening on default port
- a Sailing Analytics server, listening for HTTP requests on port 8888 and for telnet connections to the OSGi console on port 14888

Try a request to [``http://127.0.0.1:8888/index.html``](http://127.0.0.1:8888/index.html) or [``http://127.0.0.1:8888/gwt/status``](http://127.0.0.1:8888/gwt/status) to see if things worked.