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
************* TracAPI 3.0.6 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds a new feature. 
This new feature adds methods to the API, but it keeps the backward compatibility. 
This version provides a new JavaDoc version.

Release date: 11/08/2014

1) New features

 - The IRaceCompetitor implements the IMetadataContainer interface (Requested by Frank Mittag, 16/07/2014)  

2) Bugs

 - The race name and the visibility are not updated in the IRacesListener.update() method.
 (Reported by Jorge Piera, 06/08/2014)   
 - The IRaceCompetitor doesn't contains the associated IRoute object (Reported by Jorge Piera, 08/08/2014)
 - Changing the synchronization of the maps in the EventFactory (Reported by Axel Uhl, 09/08/2014)   
 
 
********************************************
************* TracAPI 3.0.5 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 05/08/2014

1) Bugs

 - The previous version 3.0.4 added only changes in the synchronization. The lists were synchronized by hand
 but the implementation continued using synchronized lists. The result was that we had a double
 synchronization for all the lists of the model: the synchronization by hand and the synchronization of
 the list. (Reported by Axel Uhl, 04/08/2014)   
 - The new subscription library sends the static positions (from the parameters file) as positions events.
 The problem here is that the subscription library retrieves the positions from the model of control 
 points and  when some races are loaded in parallel these values can be invalid (values loaded by 
 other race). Now the subscription library sends the static positions from the parameters file. 
 (Reported by Jorge Piera, 04/08/2014)  
  

********************************************
************* TracAPI 3.0.4 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 04/08/2014

1) Bugs

 - Some lists are not thread-safety. The model project exposes some lists based on a 
 CopyOnWriteArrayList implementation that is thread-safety if you get its iterator. 
 But if before to invoke the iterator() method of the list, other thread is editing the list,
 it is possible to get a list in an invalid status. (Reported by Axel Uhl, 31/07/2014)   
 

********************************************
************* TracAPI 3.0.3 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 25/07/2014

1) Bugs

 - When a race is reloaded more than one time it keeps the entries despite of they have been removed. The new entries
 are correctly added but the removed competitors are kept. (Reported by Axel Uhl, 25/07/2014)   
 
 
********************************************
************* TracAPI 3.0.2 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 24/07/2014

1) New features

 - The IRacesListener.updateRace() event is thrown when the live delay for a race has changed. The IRace.getLiveDelay() method
 returns the new value.

2) Bugs

 - The events IRacesListener.abandonRace() and IRacesListener.startTracking() have been reviewed. Now they are always sent
 when the races are updated either using the event manager or using the external JSON service "update_race_status"
 - The controls are not updated when they are retrieved a second time from a parameters file. The library always returns 
 the first control that was read the first time. (Reported by Axel Uhl, 24/07/2014)    
 
********************************************
************* TracAPI 3.0.1 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 09/06/2014

1) Bugs

 - Error reading JSON of races. 

********************************************
************* TracAPI 3.0.0 ****************
********************************************
This is a final version. 

Release date: 04/06/2014

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


 
 

