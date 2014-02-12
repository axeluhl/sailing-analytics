********************************************
******* TracTracClientModule 2.0.12 ********
********************************************

This release only contains changes in its implementation (it keeps the previous API). 

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

- Fixing a bug reading MTB files. In some scenarios when the positions of one boat arrive with a big delay, 
they can be discarded by the library. The positions are in the MTB file and it is not necessary to 
regenerate the files again. It is just a bug in the reading process. 


********************************************
******* TracTracClientModule 2.0.11 ********
********************************************

This release only contains changes in its implementation (it keeps the previous API). 

1) New features

- Fixed a deadlock in the code. If the consummer aplication invokes a method in the datacontroller,
a deadlock can happen because the stack of pending messages is locked. Now, it is always possible to
add a new message to the queue of pending messages and the system will process it as soon as posible. 

********************************************
******* TracTracClientModule 2.0.10 ********
********************************************

This release only contains changes in its implementation (it keeps the previous API). 

1) New features

- When the library loads a replay race and it finishes the loading process, it stops the
thread that is reading the stored data. If there are positions that have not been pushed to
the application, these positions won't be delivered. Now, when the loading process finishes, the library
creates a message to stops the thread and this message is add to the queue of messages and
it is delivered after all the pending messages.

- The library creates a thread per data type to load. The total progress of loading data
is a function of all the individual threads. In some scenarios can happen that one thread starts and
finishes before the rest of the threads start. In this case, the application will send a progress=100%
message and a storedStopped event (and potentially an stopped event). Some milliseconds later,
a new thread starts and it sends again the beginStored event and starts the loading progress. Now,
when a race is in replay, the initialization of the threads is done in the main thread and then, the loading
process is executed in separated threads. Using this approach, the application knows the number of
pending threads and it won't send "premature events" to the application.

- The thread for the RaceStartTime doesn't finish despite of the execution finishes. The reason is because this
thread has been started in a bad place and for races in replay mode it can continue running after the library
finishes to retrieve data. This bug has been fixed and now all the threads finish when the loading process finishes.

- When a race is loaded from an MTB file a deadlock can happen. The file is processed in blocks of data and
in some scenarios the the loop that process the blocks never finishes. This bug has been reproduced and fixed. 

********************************************
******* TracTracClientModule 2.0.9 *********
********************************************

This release only contains changes in its implementation (it keeps the previous API). 

1) New features

- A deadlock can be produced if the user application calls the DataController.stop() method. This error has
been reproduced calling the  DataController.stop() method in a thread that is executing a code
inside the DataController.Listener.progress() method. This bug has been fixed.

- When the application loads an MTB, the progress can be wrong and sometimes the library doesn't 
send the 100% progress. A bug related with the progress has been fixed. 


********************************************
******* TracTracClientModule 2.0.8 *********
********************************************
 
This release only contains changes in its implementation (it keeps the previous API). 
 
1) New features

 - Changed the way to download the file that contains the race start time. Now the donwload is done using the 
 URLConnection object and calling the setUseCaches(false) method before to create the input stream. It is an
 internal change. 
  
********************************************
******* TracTracClientModule 2.0.7 *********
********************************************

This release only contains changes in its implementation (it keeps the previous API). 
 
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





