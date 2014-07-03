********************************************
************* TracAPI **********************
********************************************

1) Content

This zip package contains 2 folders:

 - lib -> contains the Trac-API compiled library
 - src -> contains some code examples.
 
The documentation can be retrieved online from http://tracdev.dk/maven-sites-clients/3.0.0/maven-java-parent/.

It contains also some files:

 - test.sh -> script that compiles the code in the src folder, creates the test.jar library and execute the code of the example.
 - Manifest.txt -> manifest used to create the test.jar file
 
********************************************
************* TracAPI 3.0.1 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

1) Bugs

 - Error reading JSON of races. 

********************************************
************* TracAPI 3.0.0 ****************
********************************************
This is a final version. 

1) New features

 - The positions of the static marks are sent like an event
 - The static start/end tracking times and the static start/end race times are sent like an events
 - The static course is sent like an event
 - The IConnectionListener.stopped() method uses an object as argument 
 - The start/end tracking times are 0l if the race has not been initialized
 - Added the IRaceListener.startTracking event
 - Added the IRace.getExpectedRaceStartDate()
 - Added the IRace.isInitialized()
 - Added the IRace.getVisibility()
 
2) Bugs

 - Error reading the race start time from the JSON
 - Synchonizating the locators used to retrieve objects using the Service Provider Interface

********************************************
********* TracAPI 3.0.0-SNAPSHOT ***********
********************************************

This is an SNAPSHOT version that means that is a version that has not been released (is under development).
 
1) Bugs

- The route name is lost when the ControlRouteChange event is thrown
- Adding synchronized lists in some objects of the model that were not synchronized
- Fixing an error using a local parameters file with a local MTB file
- Fixing several errors marshaling the objects then an static property has been changed
- Fixing a bug parsing a JSON with races that has races without params_url (for Orienteering events)


 
 

