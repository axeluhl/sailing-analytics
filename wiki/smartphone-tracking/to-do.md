# To-Do
[[_TOC_]]

## Server
* change names of *Events, as they are not events -> also change names of abstract base classes
* use extension serializers
* exchange auto JSON to BSON conversion in MongoObjectFactory / DomainObjectFactory for something suited for productive use
* editing course in the RaceBoardAdmin
* mapping devices to marks
* generic method for registering listener for NMEA sentence types (e.g. to then process wind) -> move servlet for receiving NMEA out of smartphoneadapter
* accepting / removing competitors
 * for this we first need racelog replication back to all clients
 * also, not everybody should be able to do this -> see user management
* user management (Competitors as users, credentials so not everybody can do everything)
 * -> integrate with OAuth, ISAF competitors etc.?
* security (not everybody can start race, goes hand in hand with user management)
* support dynamic mapping of smartphone to competitor -> so that it can change during the race
* support other input channels (e.g. Igtimi)
* only transfer competitor ID for registering etc. instead of whole competitor

## Android
* use EventSendingService for all outgoing requests, not only race log
* change format of sent position Data to degrees and minutes
* reuse existing course design functionality to create RaceLogCourseDesignChangedEvent before sending RaceLogPreRacePhaseEndedEvent
* abstract sending service, so that all POST / GET requests and not only RaceLogEvents can be sent using the semi-connectedness functionality --> just write JSONObjects/Strings directly into the file. The Servlet has to handle deserialization and the client doesn't have to know what type of object it is after having saved it (is this really the case?)
* simplify settings
* login/register Activity for registering the Team and Sailor the first time the App is started