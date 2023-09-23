#A great starting point glossary...
A basic run down of *what's what*, so you can dive into the other documentation more easily. 
##The components of the project

- Apache: This is the web server technology that acts as the reverse proxy and serves the content. You can serve multiple things from the same machine. It is configured using the httpd.conf file which can be found for this project in `${GIT_HOME}/configuration/httpd/conf`. Apache works by using directives, which are commands to control access, security, sources and direct traffic, to name but a few. Apache is modular so extra config can be found in conf.d (within the configuration directory). In this directory you can find the macros used, including those used for archiving.
- EC2: This is the amazon web service that hosts the instances that the servers run on. We use load balancers to direct traffic, specifically Application Load Balancers, which routes at the HTTP/S level, making them very powerful. You might also want to read up on security groups, target groups and VPCs (virtual private clouds).
- Expedition Connector: A software package that allows us to receive wind sensor data and forward it on.
- GWT: This is the product that handles the front end and replace Python, because it allowed for one language, one editor (Eclipse) and dynamic sites. It is very modular and the details of each module are defined in the `.gwt.xml` file. Note: I had some trouble getting this working with eclipse. I used a helpful tutorial called *Modern GWT, first steps*.
- Hudson: This is a continuous integration tool, written in Java, that can execute Maven, Ant, unix and window commands. The general community now uses Jenkins and support has stopped.
- MongoDB: A NoSQL database (so there are not tables) that uses a Document model: within each database you can have multiple collections and within those are documents... Each document has a _id whic must be unique. You can access mongoDB from the terminal, using mongosh, or you can use the GUI Compass. MongoDB scales horizontally by sharding (a bit like partitioning) the documents: this is done using a field or fields to order the documents. We use it for recovery. Note: As of writing, the pacing of their own tutorials is quite slow and they abstract detail.
- OSGI: A specification for component and module based programming in Java. Equinox is an implementation of this specification, that we use. The MANIFEST.MF details the needs and output of the key components known as bundles. These can be added, removed or updated for an application whilst it is running. We use web bundles which include a web.xml descriptor in the WEB-INF which can store the static content and any servlets.
- RabbitMQ: This is a message passing service. Note: Their tutorials are great and it seems well documented. Pika Python is good for getting familiar with concepts.
- Selenium: It is a project that includes the tool we use for automated testing. 
- SwissTiming: Provides additional data and world class timing information, as well as precise mark-rounding data.
- TracTrac: The tracking gear used and the client used to interact with the backend. The Domain Factory maps the TracTrac domain to ours. The client runs a thread for receiving the race course definition, the list of competitors, the raw competitor GPS fixes, the mark positions, start/finish times and the mark rounding times.


##Some basic sailing terminology
- A **flight** or **fleet** is a subset of all the competitors. The competitors are broken up into fleets if there are too many or if there are special starting and finishing provisions. On the sapsailing.com leaderboard, the fleets are colour coded.
- A **mark** is any position a boat must pass on a required side.
- **kt** is of course knots.
- **Velocity made good** is speed towards or from the wind direction.

  
 
