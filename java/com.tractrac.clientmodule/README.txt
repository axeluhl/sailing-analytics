********************************************
******* TracTracClientModule 2.0.9 *********
********************************************

1) Content

This zip package contains 3 folders:

 - doc -> contains the javadocs
 - lib -> contains the compiled libraries
 - src -> contains some code examples.

It contains also some files:

 - test.sh -> script that compiles the code in the src folder, creates the test.jar library and execute the code of the example.
 - Manifest.txt -> manifest used to create the test.jar file
 - params.txt -> file that contains the race parameters used in the example.
 
2) New features

- A deadlock can be produced if the user application calls the DataController.stop() method. This error has
been reproduced calling the  DataController.stop() method in a thread that is executing a code
inside the DataController.Listener.progress() method. This bug has been fixed.

- When the application loads an MTB, the progress can be wrong and sometimes the library doesn't 
send the 100% progress. A bug related with the progress has been fixed. 


********************************************
******* TracTracClientModule 2.0.8 *********
********************************************
 
1) New features

 - Changed the way to download the file that contains the race start time. Now the donwload is done using the 
 URLConnection object and calling the setUseCaches(false) method before to create the input stream. It is an
 internal change. 
  
********************************************
******* TracTracClientModule 2.0.7 *********
********************************************
 
1) New features

 - Changed the way to download both the parameters file and the MTB file. Now the donwload is done using the 
 URLConnection object and calling the setUseCaches(false) method before to create the input stream. It is an
 internal change. 
 
********************************************
******* TracTracClientModule 2.0.6 *********
********************************************

1) New features

This version adds support for metadata, including updates in both the Event Manager and the TTCM. 

There are 4 types of objects that could contain metadata:

 - Races
 - Competitors
 - Waypoints/ControlPoints
 - Courses/Routes

Metadata is a free text field that can be used to add meta-information to an object. This meta-information can be any data structure (e.g, a string, a list of key-values, a xml or a html). The responsible to manage a the event has to decide the format of the metadata field. The Event Manager has been updated in order to allow the edition.

Metadata is static: the value of the metatada field is retrieved when the TTCM starts the execution and if the value is updated using the Event Manager, the TTCM has to be restarted to retrieve the new value.

2) Code examples

2.1) Get the metadata for a Race:

Event event = KeyValue.setup(paramUrl);
while (it.hasNext()) {
    Race race = (Race)it.next();
    IMetadata metadata = race.getMetadata();
}

2.2) Get the metadata for a Competitor

Event event = KeyValue.setup(paramUrl);
Iterator it = event.getCompetitorList().iterator();
while (it.hasNext()) {
    Competitor competitor = (Competitor)it.next();
    IMetadata metadata = competitor.getMetadata();
}





