# SAP Sports Server Basis

For tennis we already know that major parts of the architecture of the SAP Sailing Analytics can be used for the implementation of the Java back-end that consolidates and enriches the live data feeds coming from sources such as Hawk Eye, the WTA Umpire system and probably some "Doppler" system tracking the ball using radar. Important components and contributions from the sailing server are:

* A stable and proven Java/Equinox/Jetty environment providing a lean OSGi environment including an embedded web server

* Runtime monitoring component capable of identifying and re-starting broken or inactive bundles

* A working and proven replication infrastructure enabling long-distance replication and scale-out support

* Proven ways to manage a target platform, including a useful target platform definition for the most common frameworks such as Apache Commons and JAX-RS

* Maven / Tycho / Hudson/Jenkins life cycle management environment supporting build, (local/hot/remote) deploy, server restart

* Google Web Toolkit (GWT) fitted into the OSGi and build architecture, supporting structured code sharing across OSGi back-end and GWT client

* Android support including code sharing between OSGi back-end and Android client

* Concurrency support in the form of various locking and caching components

* Growing Amazon EC2 support, enabling elastic scaling of a solution

Similarly, for supporting equestrian sports the same architecture can be used in case sensor data is to be processed, analyzed and visualized in a way similar to what we do for sailing.

We should therefore consider factoring out the commonalities of our platform and give it a name, such as the "SAP Sports Server" which then would live in its own git, have its own life and release cycle and deliver to projects such as sailing, tennis ans equestrian sports.