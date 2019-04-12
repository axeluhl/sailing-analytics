********************************************
************* TracAPI **********************
********************************************

1) Content

This zip package contains 2 folders:

 - lib -> contains the Trac-API compiled library
 - src -> contains the source code of the API and some code examples  
 
The documentation can be retrieved online from http://tracdev.dk/maven-sites-clients/3.0.0/maven-java-parent/.

It contains also some files:

 - test.sh -> script that compiles the code in the src folder, creates the test.jar library and execute the code of the example.
 - Manifest.txt -> manifest used to create the test.jar file

********************************************
************* TracAPI 3.12.0 ***************
********************************************
This is a final version. It changes the enumerate RaceStatusType,
breaking the backward compatibility:

 - The new RaceStatusTypes requested by Swiss Timing are:
    - NONE (NONE)
    - START (STR_SEQ)
    - RACING (RAC)
    - FINISHED (FSHD)
    - ABANDONED (ABDN)
    - FINAL (FINAL)
    - GENERAL_RECALL (GNR_REC)
    - POSTPONED (PSP)
 - TracAPI establishes equivalences with the old and the new statuses:
    - PENDING -> NONE
    - START_PHASE -> START
    - RUNNING -> RACING
    - CANCELLED -> ABANDONED
    - UNSCHEDULED -> NONE
    - PRESCHEDULED -> NONE
    - SCHEDULED -> NONE
    - FINISHING -> RACING

Release date: 27/03/2019
Build number: f52717c06bb047a747030e3ea450e15b609edf73

 1) Features

 - Updating the list of race statuses (Requested by Radek Masnika, 01/03/2019)


********************************************
************* TracAPI 3.11.1 ***************
********************************************
This is a final version.It fixes bugs in the implementation.
It keeps the backward compatibility:

Release date: 19/10/2018
Build number: 6c0010208212c2aad1016bd69708bc6859e371e9

 1) Bugs

 - Validating null values for the methods of the IRaceSubscriber that accept an array of
 Classes (Reported by Andre Borud, 05/09/2018)
 - The IEventSubscriber is not able to connect to the server if the IEvent has been created
  using the IEventFactory.createEvent(URL) method and the URL uses HTTPs. It only works using HTTP
  (Reported by Thomas Scott, 19/10/2018)

********************************************
************* TracAPI 3.11.0 ***************
********************************************
This is a final version. It changes the implementation of the IRaceSubscriber interface,
breaking the backward compatibility:

 - The methods IRaceSubscriber.subscribeRoutes() and IRaceSubscriber.unsubscribeRoutes()
 have been removed.
 - The IRoutesListener has been removed.
 - The IControlRouteChangeListener has been extended adding a new method to get updates
 of a IPathRoute. If you are managing "sailing events" this method will never be invoked.
 - When any of the attributes of the route is updated, will be notified using the
 IRaceSubscriber.subscribeRouteChanges(IControlRouteChangeListener) method

Release date: 30/07/2018
Build number: 416b827959b696583cce509ac39b7fd64aaefe9b

 1) Features

 - Merging the subscriptions IRaceSubscriber.subscribeRouteChanges and IRaceSubscriber.subscribeRoutes
 in the same method (Requested by Axel Uhl, 23/07/2018)

 2) Bugs

  - When the client is offline, if it tries to load a race it gets a RaceLoadingException. But then,
  when the client is online again, it can't reuse the same race because it is in an internal wrong
  status (Reported by Thomas Scott, 19/06/2018)

********************************************
************* TracAPI 3.10.1 ***************
********************************************
This is a final version.It fixes bugs in the implementation and it adds a some features.
It keeps the backward compatibility:

 Release date: 28/06/2018
 Build number: 35a26e503339fff26fe984617e5190a18438e10b

  1) Features

  - Adding a method to retrieve positions by class id (Requested by Chris Terkelsen, 07/05/2018)

  2) Bugs

 - When the client is offline, if it tries to load a race it gets a RaceLoadingException. But then,
 when the client is online again, it can't reuse the same race because it is in an internal wrong
 status (Reported by Thomas Scott, 19/06/2018)
 - The live delay is not propagated (sometimes). This feature has been reimplemented and now the
 value is attached and sent with the race object using the same approach used to transmit the race
 start time or the tracking start time (Reported by Axel Uhl, 19/06/2018)

********************************************
************* TracAPI 3.10.0 ***************
********************************************
This is a final version. It changes the implementation of the ISensorData interface, breaking the
backward compatibility:

Release date: 02/03/2018
Build number: a0cbb8116ab0917334b9b323db6664671a05f79b

 1) Features

 - Updating the interface ISensorData (Requested by Axel Uhl, 01/03/2018)


********************************************
************* TracAPI 3.9.0 ****************
********************************************
This is a final version. It changes the implementation of the ISensorData interface, breaking the
backward compatibility:

Release date: 01/03/2018
Build number: 8fdd19ee81b3ccbfc2694963e8e398346eebde7a

 1) Features

 - Updating the interface ISensorData (Requested by Axel Uhl and Jakob Odum, 22/11/2017)
 - Support for https downloading MTBs (Requested by Axel Uhl, 07/01/2018)

********************************************
************* TracAPI 3.8.0 ****************
********************************************
This is a final version. It change the signature of a method, breaking the
backward compatibility:

- com.tractrac.model.lib.api.event.RaceStatusType enum. UNKNOWN value of this enum type has been renamed to PENDING.

This version provides a new JavaDoc version.

Release date: 22/11/2017
Build number: 14453

 1) Features

 - com.tractrac.model.lib.api.event.RaceStatusType enum. UNKNOWN value of this enum type has been renamed to PENDING.
 (Requested by Chris Terkelsen 24/10/2017)
 - Support for sensor data. IRaceSubscriber has been updated adding methods to retrieve sensor data (Requested by SAP)
 - When the tracking of a race is initialized, TracAPI also sends the positions of the controls using the tracking start time
 as timestamp (Requested by Axel Uhl, 14/11/2017)


********************************************
************* TracAPI 3.7.7 ****************
********************************************
This is a final version. It fixes bugs in the implementation
It keeps the backward compatibility.

Release date: 29/10/2017
Build number: 14453

 1) Bugs

 - Selector.wakeup() throws an IOException in Android 5. It seems a bug on this platform
 (Reported by André Borud, 30/08/2017)
 - There is a synchronization bug on the implementation of the IAttachable interface
 (Reported by Axel Uhl, 29/10/2017)


********************************************
************* TracAPI 3.7.6 ****************
********************************************
 This is a final version.It fixes bugs in the implementation and it adds a some features.
 It keeps the backward compatibility.

 Release date: 14/08/2017
 Build number: 14241

 1) Features

 - Two new RaceCompetitorStatusType values have been added: 8 (Did not finish) and 9 (Missing data). (Requested by
 Chris Terkelsen, 28/07/2017)

 2) Bugs

 - Releasing the memory when the ISubscriber.close method is called (Reported by Axel Uhl, 04/08/2017)
 - The live messages (e.g: race start time updated, live delay updated...) are not arriving sometimes,
 despite of the server is sending them all the time. The implementation of this part has been
 downgraded to the version used in TracAPI 3.6.x (Reported by SAP)


********************************************
************* TracAPI 3.7.5 ****************
********************************************
This is a final version. It fixes bugs in the implementation
It keeps the backward compatibility.

Release date: 25/07/2017
Build number: 14214

 1) Bugs

 - When the library calls the IRaceStartStopTimesChangeListener methods,
   the IRace object can be not updated (Reported By Axel Uhl, 25/07/2017)

********************************************
************* TracAPI 3.7.4 ****************
********************************************
This is a final version. It fixes bugs in the implementation
It keeps the backward compatibility.

Release date: 25/07/2017
Build number: 14207

 1) Bugs

 - It is not possible to connect a second time to the ISubscriber object to receive
 live notifications if it has been closed before (Reported By Axel Uhl, 24/07/2017)

********************************************
************* TracAPI 3.7.3 ****************
********************************************
This is a final version. It fixes bugs in the implementation
It keeps the backward compatibility.

Release date: 24/07/2017
Build number: 14195

 1) Bugs

 - When the specific live delay is updated, the EM sends a message with the new and the
  old values to the clients. TracAPI checks if the cached value is equals to the old value and
  only in this case, it updates the live delay. If the server sends some wrong values or if a message
  is lost, the live delay will never be updated and for this reason this validation has been
  removed (Reported By Axel Uhl, 24/07/2017)

********************************************
************* TracAPI 3.7.2 ****************
********************************************
This is a final version. It fixes bugs in the implementation
It keeps the backward compatibility.

Release date: 21/07/2017
Build number: 14174

 1) Bugs

 - IRace.getCourseArea() is not updated when the race is loaded a second time (Reported By Axel Uhl, 21/07/2017)

********************************************
************* TracAPI 3.7.1 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds a some features.
It keeps the backward compatibility.

Release date: 21/07/2017
Build number: 14172

 1) Features

 - RaceStatusType includes a new value: 9 - Final. The meaning of this value is that the related race has finished and the
 splits have been validated (Requested by Chris Terkelsen, 18/07/2017).

 2) Bugs

 - NullPointerException when the client stops the subscription during the loading progress (it happens sometimes).
 The data provider is set to null and one of the threads that sends the positions to the app can use it (Reported
 By André Borud, 03/07/2017)
 - IRace.getEvent() is null when the race is updated (Reported By Axel Uhl, 21/07/2017)

********************************************
************* TracAPI 3.7.0 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds some new features.
These features add methods to the API keeping the backward compatibility with the releases
3.6.x. The minor version has been increased from 6 to 7 because there are some important
changes in the implementation.

This version provides a new JavaDoc version.

Release date: 28/07/2017
Build number: 14122

 1) Features

 - RaceCompetitorStatusType: New race competitor status type defined: F_CONFIRMED. Represents a competitor with a
 control passing registered for the last gate/mark that is accepted as the official result (Requested by Ugo
 Alvazzi, 26/04/2017)
 - WebSockets support: instead of using a TCP connection is possible to retrieve data using WebSockets (using the
 same protocol used by the HTML Viewer). The protocol is not fully implemented and it only has to be used for testing
 (Requested by Jorge Piera, 30/04/2017).
 - Use an asynchronous API: the dataserver implementation has been migrated to an asynchronous API that provides
 a better performance downloading replay data (the tests show that is 2.5 times faster). TracAPI has also adopted
 this new asynchronous interface but at this moment it continues using the synchronous approach (Requested by Jorge
 Piera, 03/05/2017)
 - Adding the method ISubscriberFactory.clean to release the objects in memory (Requested by André Borud, 01/06/2017)
 - Adding the method IPosition.isGPSTiming to know is the time of a position has been retrieved from a physical
 GPS device (Requested by Jérome Soussens, 21/06/2017)

 2) Bugs

 - When a static control is updated, the IControlPointPositionListener.gotControlPointPosition event
 is not called if the consumer application is not registered to the IEventSubscriber (Reported by
 Axel Uhl, 18/06/2017)
 - The live delay sometimes is not propagated. The server always propagates the event but TracAPI can
 filter it if the IEventSubscriber is not being used or if it has not been correctly initialized
 using the right live-uri (Reported by Julian Mayland, 21/06/2017)

********************************************
************* TracAPI 3.6.3 ****************
********************************************
This is a final version. It adds support for these new features:

 - IMetadataContainer: Fixed bug in IMetadataContainer implementation. Metadata received through serialized objects was not being parsed.

Release date: 10/04/2017
Build number: -

********************************************
************* TracAPI 3.6.2 ****************
********************************************
This is a final version. It adds support for these new features:

 - IControlPoint.getId() will return null when the control point is part of a line and the related IControl do not have
   the properties P1.UUID/P2.UUID defined.

Release date: 06/04/2017
Build number: -

********************************************
************* TracAPI 3.6.1 ****************
********************************************
This is a final version. It adds support for these new features:

 - IControl and IRace now have a method getCourseArea() that returns the course area.
 - IControlPoint has a getId method that returns the UUID. It will be initialized with the value set in the P1.UUID and
  P2.UUID properties defined in the parent IControl. In case these properties are not defined the UUID will fallback to
  the parent IControl UUID.

Release date: 04/04/2017
Build number: 13554

This version provides a new JavaDoc version.

********************************************
************* TracAPI 3.6.0 ****************
********************************************
This is a final version. It change the signature of a couple of methods, breaking the
backward compatibility. These changes are:

 - The IEventFactory.createRace(URI) doesn't throw the TimeOutException.
 - The IEventFactory.createRace(URI, URI, URI) doesn't throw the TimeOutException.

This version provides a new JavaDoc version.

Release date: 08/03/2017
Build number: 13498

 1) Features

 - Removing the TimeOutException of some methods of the IEventFactory (Requested by Axel Uhl, 08/03/2017)

********************************************
************* TracAPI 3.5.0 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds some new features.
These features add methods to the API breaking the backward compatibility. These changes are:

 - The IRaceCompetitor interface changes the return type of the getStatusTime method
  from Date to long.
 - The methods IEventFactory.createRace throw now a TimeOutException when there is a timeout
  downloading the parameters file.

This version provides a new JavaDoc version.

Release date: 07/03/2017
Build number: 13490

 1) Features

 - Updating the return type of the IRaceCompetitor.getStatusTime from Date to long to
 be consistent with the API (Requested by Jorge Piera, 13/02/2017)
 - Adding the methods ISegment.getExtent(), the IRoute.getExtent() and IRace.getExtent()
 that return the extent of the object (Requested by Jorge Piera, 23/02/2017)
 - Adding an extra parameter to the IEventFactory.createRace methods to indicate
 the timeout used to download the parameters file. The IEventFactory also includes a method
 getDefaultTimeOut that returns the default timeout (Requested by Axel Uhl, 06/03/2017)

********************************************
************* TracAPI 3.4.1 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 20/02/2017
Build number: 13415

 1) Bugs

 - TracAPI doesn't send all data when the races are retrieved in parallel.
 It is an error in the thread that executes the callback messages that in some
 internal circumstances, doesn't execute the callback (Reported by Axel Uhl,
 13/02/2017)

********************************************
************* TracAPI 3.4.0 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds some new features.
These features add methods to the API breaking the backward compatibility. These changes are:

 - The IRacesListener interface implements a new method dataSourceChanged. If
 your app implements this interface, you have to implement this extra method.

This version provides a new JavaDoc version.

Release date: 31/01/2017
Build number: 13339

1) Features

 - Adding the method IRace.getDataSource that returns the datasource used by the race
 to load data (Requested by Axel Uhl, 25/01/2017)
 - Adding the method IRacesListener.dataSourceChanged that sends a notification
 every time that the datasource of a race changes (Requested by Axel Uhl, 25/01/2017)

2) Bugs

 - Call IRaceSubscriber.unsubscribePositions() after IRaceSubscriber.start() generates a NullPointerException
 (Reported by Jerome Soussens, 24/01/2017)


********************************************
************* TracAPI 3.3.1 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds some new features.
These features add methods to the API, but they keep the backward compatibility.
This version provides a new JavaDoc version.

Release date: 17/01/2017
Build number: 13262

1) Features

 - When a race is in live, the library starts to download stored data from the time when the library connects
  allowing to the user show data as fast as the first data stream arrives. Then, the library starts to download
  the rest of the old data. (Requested by Jakob Odum, 25/09/2016)
 - Adding the method IEventFactory.deleteCache that removes all the previous cached objects (Requested by Andre
  Borud, 14/12/2016)
 - Changing the synchronization of the UtilLocator.createParamerSet method. It was synchronized at method level
  but it is only necessary to synchronize the initialization of the factory and not the usage (Requested by Axel Uhl,
  17/01/2017)

2) Bugs

 - The ICompetitorClass implementation can contain duplicate entries of different implementations. (Reported by
 Juan Salvador Pérez, 16/01/2017)

********************************************
************* TracAPI 3.3.0 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds some new features.
These features add methods to the API breaking the backward compatibility. These changes are:

Release date: 21/09/2016
Build number: 12935

1) Features

 - Adding the method IRaceCompetitorListener.updateRaceCompetitor that is thrown when a race
 competitor is updated (Requested by Jerome Soussens, 21/09/2016)

********************************************
************* TracAPI 3.2.2 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds a some features.
These features add methods to the API, but they keep the backward compatibility.
This version provides a new JavaDoc version.

Release date: 21/09/2016
Build number: 12927

1) Features

 - Adding the method ISubscriber.isRunning() to know if the thread created by the ISubscriber.start() method
 is running or not (Requested by Jorge Piera, 04/08/2016).

 - Added attribute in the IRaceCompetitor interface: statusTime. It will contain the time when an entry has changed
 its status to abandoned/retired.

 - Added new value to the RaceCompetitorStatusType enum: NO_COLLECT. This value means that the competitor didn't
 collected the tracker.

2) Bugs

 - When an static control is added (or updated), the timestamp has to be the event start time. At this moment
  it is using the timestamp when whe control was created (Reported by Steffen Wagner, 12/09/2016)


********************************************
************* TracAPI 3.2.1 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 13/07/2016
Build number: 12627

1) Bugs:

 - If the Internet connection goes down, some threads can be hung despite of
 the consumer application calls the ISubscriber.stop method (Reported by
 Axel Uhl, 13/07/2016)

********************************************
************* TracAPI 3.2.0 ****************
********************************************
This is a final version. New functionality added:

Release date: 05/06/2016
Build number: 12293

1) Features:

 - IRace adds the new property, status, returning a value of the enum RaceStatusType.

 - IRace property visibility has changed to return a value of the enum RaceVisibilityType.

 - IRaceCompetitor adds a new property: status of type RaceCompetitorStatusType.

 - Competitor updates during a race will be propagated to all subscriptors.

********************************************
************* TracAPI 3.1.6 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 22/02/2016
Build number: 11976

 1) Bugs
 
 - The bug of the previous release (3.1.2) was not fixed.
 
********************************************
************* TracAPI 3.1.5 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 17/02/2016
Build number: 11948

 1) Bugs
 
 - The bug of the previous release (3.1.2) was not fixed.
 
********************************************
************* TracAPI 3.1.4 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 15/02/2016
Build number: 11938

 1) Bugs
 
 - An infinity loop has been detected in the thread that sends the application
 messages (Repored by Axel Uhl, 12/02/2016) 

********************************************
************* TracAPI 3.1.3 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 25/01/2016
Build number: 11888

 1) Bugs
 
 - The bug of the previous release (3.1.2) was not fixed.

********************************************
************* TracAPI 3.1.2 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 21/01/2016
Build number: 11864 

 1) Bugs
 
 - The ISubscriber.stop() method never finishes due to a deadlock (it happens sometimes).
 There is a deadlock between one of the internal threads responsible to update the progress 
 and the thread that calls the stop method. The release 3.1.1 fixed a part of the bug but
 it continues happening due to a new deadlock (Reported by Axel Uhl, 20/01/2016) 

********************************************
************* TracAPI 3.1.1 ****************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 18/01/2016
Build number: 11844

 1) Bugs
 
 - The ISubscriber.stop() method never finishes due to a deadlock (it happens sometimes).
 There is a deadlock between one of the internal threads responsible to update the progress 
 and the thread that calls the stop method (Reported by Axel Uhl, 16/11/2015) 

********************************************
************* TracAPI 3.1.0 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds some new features. 
These features add methods to the API breaking the backward compatibility. These changes are:

 - The IRaceCompetitorListener interface implements a new method removeOffsetPositions. If 
 your app implements this interface, you have to implement this extra method.  
 
This version provides a new JavaDoc version.

Release date: 11/12/2015
Build number: 11801

 1) Features
 
   - Adding the method IRaceCompetitorListener.removeOffsetPositions that is invoked
  to invalidate a set of positions. It is used when the event administrator detects 
  an error in the snapping algorithm (in route sports) and he want to remove the wrong
  positions (Requested by Chris Terkelsen, 26/11/2015)
 
  - Adding the method IControl.getMapName() that contains the name that has to be used to
  display the control on the map (Requested by Chris Terkelsen, 09/12/2015)
    
 2) Bugs
 
  - The static controls send a position event using the race start time instead the 
  event start time. (Reported by Jerome Soussens, 11/12/2015)
   
********************************************
************* TracAPI 3.0.14 ***************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 29/09/2015
Build number: 11514

 1) Bugs
 
 - NullPointerException caused by a synchronization error (Reported by Jérome Soussens, 28/09/2015)
 
********************************************
************* TracAPI 3.0.13 ***************
********************************************
This is a final version. Only fixes bugs in the implementation

Release date: 23/09/2015
Build number: 11448

 1) Features
 
 - The script that generates the final version also includes a jar with the source code of the APIs
 (Requested by Axel Uhl, 07/09/2015)

 2) Bugs
 
 - When a new race is updated, the IRace.getParamsURI() is updated to null. (Reported by Juan Salvador Pérez, 10/09/2015)
  
 - The IRace.getMetadata(), ICompetitor.getMetadata and IControl.getMetadata() methods are not refreshed in
 the model when the objects are updated in the event manager (Reported by Jorge Piera, 23/09/2015)
 
********************************************
************* TracAPI 3.0.12 ***************
********************************************
This is a final version. It fixes bugs in the implementation and it adds a some features. 
These features add methods to the API, but they keep the backward compatibility. 
This version provides a new JavaDoc version.

Release date: 07/08/2015
Build number: 11130

 1) Features
 
 - Adding the method ISubscriberFactory.setUserId(userId) used to configure a valid user with permissions
 to retrieve data from the TracTrac servers. (Requested by Jorge Piera, 18/06/2015)
 
 2) Bugs
 
 - If the consumer application is not subscribed to the events that manage the addition of new "objects" 
 in the system (e.g: add control, add competitor...), it is never going to receive the events related 
 with these "objects". It is an error of design in TracAPI because it has been designed "thinking"
 that the consumer application is always subscribed to receive all the events. The solution is simple: 
 TracAPI is always going to subscribe to all the events to guarantee that the static model is always 
 updated and to guarantee that if the consumer application is subscribed to a type of event it is always 
 going to receive all the events of this type. There is a secondary effect: the consumer application can 
 receive events with new objects. (Reported by Axel Uhl, 20/07/2015)
  
********************************************
************* TracAPI 3.0.11 ***************
********************************************
This is a final version. It fixes bugs in the implementation and it adds a some features. 
These features add methods to the API, but they keep the backward compatibility. 
This version provides a new JavaDoc version.

Release date: 27/05/2015
Build number: 10715

 1) Features 
 
 - Adding the method IRace.setInitialized(boolean) that allows to load control point positions despite of
 the race is not initialized (Requested by Jorge Piera, 17/05/2015)
 
 1) Bugs 
 
  - The subscription library sends (per each control point) an "ControlPointPosition" event with the positions 
 of the parameters with a time stamp equals to the tracking start time. If the control  point is static (it 
 doesn't have a tracker), it means that this control won't exist until the tracking start time arrives: it is 
 not possible to open a subscription connection before the tracking start time and see the control point. But 
 if the control is static,  it has to exist during all the event. Now when a control is static, the subscription 
 library sends a "ControlPointPosition" event with the event start time and then it sends other "ControlPointPosition"
 event with the tracking start time. It allows to connect with future races and see the static controls (Requested 
 by Jakob Odum, 25/02/2015)
   
 - It is possible to add two or more subscriptions for the same subscriber (Reported by Jorge Piera, 02/05/2015)
 
 - An initialized race is a race where the tracking is on. A not initialized race is a race where the tracking is off.
 If a race is in a not initialized state and it is loaded using the parameters file, the library assumes that the 
 consumer application wants to load the race and it changes its initialized  attribute to true (the tracking is on).
 This assumption is wrong. Now, if a race is not initialized it will continue being uninitialized until the event 
 administrator changes its state to initialized.
   When a race changes its state from not initialized to initialized, some events are thrown in the system:
     1. IRaceStartStopTimesChangeListener.gotTrackingStartStopTime(IRace, IStartStopData): where the IStartStopData object contains the new tracking interval.
     2. IRacesListener.startTracking(UUID): the UUID is the race identifier
     3. IRacesListener.updateRace(IRace): IRace is the new updated race where the IRace.isInitialized is true and it has values for both the getTrackingStartTime and the getTrackingEndTime methods.
   When a race changes its state from initialized to not initialized, the following events are thrown in the system:
     1. IRaceStartStopTimesChangeListener.gotTrackingStartStopTime(IRace, IStartStopData): where the IStartStopData object contains the null interval ([0, 0])
     2. IRacesListener.abandonRace(UUID): the UUID is the race identifier
     3. IRacesListener.updateRace(IRace): IRace is the new updated race where the IRace.isInitialized is false and it has null values for both the getTrackingStartTime and the getTrackingEndTime methods.
  (Reported by Axel Uhl, 17/05/2015)
 
********************************************
************* TracAPI 3.0.10 ***************
******************************************** 
This is a final version. Only fixes bugs in the implementation

Release date: 17/02/2015
Build number: 9938
 
 1) Bugs
 
 - It fixes the same bug described in the release 3.0.9 that it was not fixed. The release 3.0.9 fixed
 the synchronization issue between different threads but the ConcurrentModificationException happens
 when the same thread is accessing to the list. The synchronization at method level doesn't work
 in this case (Reported by Axel Uhl,07/02/2015) 
 
********************************************
************* TracAPI 3.0.9 ****************
******************************************** 
This is a final version. Only fixes bugs in the implementation

Release date: 13/02/2015
Build number: 9904
 
 1) Bugs
 
 - There is a ConcurrentModificationException in the IConnectionStatusListener subscription, when a thread
 is adding subscriptions/unsubscriptions and other thread is sending events through this listener (Reported 
 by Axel Uhl,07/02/2015)

********************************************
************* TracAPI 3.0.8 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds a some features. 
These features add methods to the API, but they keep the backward compatibility. 
This version provides a new JavaDoc version.

Release date: 07/02/2015
Build number: 9826
 
 1) New features
 
 - Adding the method IEvent.getEventType that returns the type of the event (e.g: Sailing, Orienteering...).
 (Requested by Jorge Piera, 01/02/2014)
 
 2) Bugs
 
 - The changes in the static fields of the route (including the metadata) were not propagated through 
 the subscription library. Following the same approach used for the races, controls and competitors, 
 a new listener IRoutesListener has been added to manage these changes.  (Reported by Axel Uhl,
 06/02/2015)
 
********************************************
************* TracAPI 3.0.7 ****************
********************************************
This is a final version. It fixes bugs in the implementation and it adds a some features. 
These features add methods to the API, but they keep the backward compatibility. 
This version provides a new JavaDoc version.

Release date: 19/11/2014
Build number: 9436

1) New features

 - The IRace interface implements the method getStartTimeType that returns the type of start time. It is an enumerated 
 value with the values: Individual (if the competitors have an individual start time), RaceStart (if the race
 has a race start time) and FirstControl (if the start of the race happens when the competitor passes for
 the first control). The value by default is RaceStart. At this moment this value only can be managed at an event
 level (in the event manager) but in a future we will add this functionality at a race level (Requested by 
 Jérome Soussens, 14/10/2014)  
 - The method IControlPoint.getPosition is deprecated. Use the IControlPointPositionListener.gotControlPointPosition()
 to know the position of a control point (Reported by Jorge Piera, 28/10/2014)   
 - Adding the isNonCompeting() method to the ICompetitor interface. If a competitor is a non competing competitor
 doesn't receive control passings attached to it. It only receives positions (Requested by Jérome Soussens, 15/10/2014 and
 Axel Uhl, 22/10/2014) 

2) Bugs

 - It checks that the system of messages doesn't generates a BufferOverflow exception. We added a bug on the server
 sending wrong strings to the clients and this error thrown an exception on the clients that killed one of threads.
 Now the tracapi checks if there is an error encoding the strings and in this case, logs the exeption discarding
 the message but the thread, continues running. Anyway the error has been fixed in the server side. (Reported by 
 Jorge Piera, 30/09/2014)   
 - If there is a control point without coordinates the lat,lon values are not included in the parameters file
 and the library doesn't create the IControlPoint. Now, the library creates the IControlPoint and it returns
 the values Double.MAX_VALUE for both the lat and the lon. This position is not sent by the 
 IControlPointPositionListener.gotControlPointPosition(). (Reported by Jakob Odum, 28/10/2014)    
 - There is a synchronization bug registering and sending messages using the General Message System. The list
 that is used to register the listeners is not a synchronized list. This bug has been fixed (Reported by Axel Uhl, 19/11/2014,
 http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2474)
 
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


 
 

