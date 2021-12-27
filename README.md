# SAPSailingAnalytics
The SAP Sailing Analytics platform as seen on [sapsailing.com](https://sapsailing.com)

Sailing provides the perfect platform for SAP to showcase solutions and help the sport run like never before. SAP’s involvement in the sport has transformed the sailing experience by providing tools, which:

Help sailors analyze performance and optimize strategy
Bring fans closer to the action
Provide the media with information and insights to deliver a greater informed commentary
SAP has a longstanding involvement with sailing and has established a portfolio spanning across teams and regattas, including:

Technical Partner of the European Sailing Leagues
Partner of the world’s largest regatta, Kieler Woche (Kiel Week)
Technical Partner of the Sailing Champions League
Title sponsor of the SAP 5O5 World Championships
Official Technology Partner of WORLD SAILING

Start contributing: [https://wiki.sapsailing.com/wiki/howto/onboarding](https://wiki.sapsailing.com/wiki/howto/onboarding)

To build, invoke
```
    configuration/buildAndUpdateProduct.sh build
```
To build a docker image, try ``docker/makeImageForLatestRelease``. To run that docker image, try something like
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
