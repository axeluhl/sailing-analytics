# Tracking App API v1

## General

This is a design for the REST API to be used by the iOS and Android tracking apps.
We have decided to handle checkins etc. at the level of ``Leaderboard``s instead of ``Regatta``s. For most cases, such a Leaderboard is based on a Regatta (a ``RegattaLeaderboard`` in our domain model).

This document supplements the [documentation of the existing REST API](http://www.sapsailing.com/sailingserver/webservices/api/v1/index.html), highlighting those resources necessary for the tracking app, and specifying such resources that are not yet present in the API.

For the moment, this document also contains the API for the proposed [Buoy Tender (Tonnenleger) App](#buoy-tender-app), which can be moved to a separate document at some point.

### URL base
```
http://<host>/sailingserver/api/v1/
```
All relative URLs given below are relative to this base URL.

## Process

The endpoints listed below should mostly be called in the order they are listed here.
That is, after

1. receiving the [Checkin Information](#tracking-checkin-info), the App can 
2. then perform the [Check-In](#checkin) for the competitor in question
3. and proceed with sending [GPS Fixes](#fixes).


## Push Notifications

_not yet implemented_

Event data can be updated via push notifications. These are limited on iOS to 256 bytes (not characters). For this reason, on receiving a push notification, the app must GET the event data.

## Check-In Information
<div id="tracking-checkin-info"></div>

### URL and QRCode

We have the following structure of domain objects (look here for [API documentation](http://www.sapsailing.com/sailingserver/webservices/api/v1/index.html) in addition to the examples):
* Event (e.g. [Kieler Woche 2013](http://www.sapsailing.com/sailingserver/api/v1/events/3e4145a7-5473-4670-83e6-fd5ee364f7eb))
  * LeaderboardGroup (e.g. [Kieler Woche 2013](http://www.sapsailing.com/sailingserver/api/v1/leaderboardgroups/Kieler%20Woche%202013))
     * Leaderboard (e.g. [KW 2013 Olympic (2.4M)](http://www.sapsailing.com/sailingserver/api/v1/leaderboards/KW%202013%20Olympic%20(2.4M%29))
         * Race (caution: this is over-simplified)

These resources have separate life cycles, the hierarchical presentation only denotes references between the resources (e.g. an Event references some LeaderboardGroups).

The app has to receive the following information:
* ``event-id`` (for description, start- and end-date)
* ``leaderboard-name`` (so that the user can checkin for the correct leaderboard)
* either of
    * ``competitor-id`` (so the app can register the device for the correct competitor)
    * ``boat-id`` (so the app can register the device for the correct boat)
    * ``mark-id`` (so the app can register the device for the correct mark)

This information is represented in a URL with one of the following structures:

```
http://<host>/tracking/checkin
  ?event_id=<event-id>
  &leaderboard_name=<leaderboard-name>
  &competitor_id=<competitor-id>
```

```
http://<host>/tracking/checkin
  ?event_id=<event-id>
  &leaderboard_name=<leaderboard-name>
  &boat_id=<boat-id>
```

```
http://<host>/tracking/checkin
  ?event_id=<event-id>
  &leaderboard_name=<leaderboard-name>
  &mark_id=<mark-id>
```

**Additional Notes:**
* The URL is **only symbolic**. Requesting it will for now result in a 404. In the future, it might be used to redirect to the .apk, play store or app store in case the app is not installed.
* The variable-length leaderboard name is problematic when thinking about length restrictions in QRCodes anyway. We will have to think about URL-shortening anyhow, so legible parameter names were chosen instead of short ones (e.g. ``event_id=`` instead of ``e=``)
* The app has to get event and competitor data by performing additional REST API calls
* The returned JSON-document for an event includes the names of the associated LeaderboardGroups. The JSON for the LeaderboardGroup in turn includes the names of the associated leaderboards. This structure of the returned resources does not follow the [REST constraints](http://en.wikipedia.org/wiki/Representational_state_transfer#Uniform_interface) in the sense that it does not use URIs to refer to associated resources. Instead, only the unique identifier is given, and one has to refer to the API documentation to construct the URI through which the related resource can be addressed.

### Event Information

see [events/{event_id}](http://www.sapsailing.com/sailingserver/webservices/api/v1/eventGetDoc.html)

### Leaderboard Information

see [leaderboards/{leaderboard_name}](http://www.sapsailing.com/sailingserver/webservices/api/v1/leaderboardGetDoc.html)

### Competitor Information (in general)

_**Note:** Consider using the leaderboard-dependent verison below - which includes the ``displayName``._

**Path:** ``competitors/{competitor-id}``

**Verb:** ``GET``

**Response:**
```
{
  "name": "Heiko KRÖGER",
  "id": "af855a56-9726-4a9c-a77e-da955bd289be",
  "sailID": "GER 1",
  "nationality": "GER",
  "boatClassName": "49er",
  "countryCode": "DE"
}
```

**Additional Notes:**
* Competitor profile image left out for now.
* No flag image included. These images are present on the server in the GWT-context only as 16x11 pngs currently. How flags are displayed in the app should probably be independent. I would suggest resolving the appropriate image within the app based on the ``countryCode``. The images are exposed through the the static endpoint ``<host>/gwt/images/flags/<countryCode>.png`` ([example](http://www.sapsailing.com/gwt/images/flags/de.png))

### Competitor Information (Leaderboard-specific)

**Path:** ``leaderboards/{leaderboard-name}/competitors/{competitor-id}``

**Verb:** ``GET``

**Response:**
```
{
  "name": "Heiko KROEGER",
  "displayName": "Heiko KRÖGER",
  "id": "af855a56-9726-4a9c-a77e-da955bd289be",
  "sailID": "GER 1",
  "nationality": "GER",
  "boatClassName": "49er",
  "countryCode": "DE"
}
```

**Additional Notes:**
* The semantics of ``displayName`` are explained in the JavaDoc of ``Leaderboard#getDisplayName()``.


## Check-In
<div id="checkin"></div>

After the user has received the event data with the short URL, the smartphone is mapped to a competitor.

Result: fixes transmitted after the time specified in the checkin request are added to such races within the leaderboard that have a start/end time matching the fixes timestamp.

_Question: What should be done if a second device is mapped to competitor?_
* _Answer:_ For now, according to our internal logic, both devices are actually mapped. If they send fixes for the same timerange, we will see some weird behaviour.

**Path:** ``leaderboards/{leaderboard-name}/device_mappings/start``

**Verb:** ``POST``

**Request:**

```
{
    "competitorId" : "af855a56-9726-4a9c-a77e-da955bd289bg",
    "deviceType" : "ios",
    "deviceUuid" : "af855a56-9726-4a9c-a77e-da955bd289bf",
    "pushDeviceId" : "0f744707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bbad78",
    "fromMillis" : 1415624564718
}
```

```
{
    "boatId" : "af855a56-9726-4a9c-a77e-da955bd289bg",
    "deviceType" : "ios",
    "deviceUuid" : "af855a56-9726-4a9c-a77e-da955bd289bf",
    "pushDeviceId" : "0f744707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bbad78",
    "fromMillis" : 1415624564718
}
```

```
{
    "markId" : "af855a56-9726-4a9c-a77e-da955bd289bg",
    "deviceType" : "ios",
    "deviceUuid" : "af855a56-9726-4a9c-a77e-da955bd289bf",
    "pushDeviceId" : "0f744707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bbad78",
    "fromMillis" : 1415624564718
}
```

* either of:
    * **competitorId** ID of the competitor to map the device to.
    * **boatId** ID of the boat to map the device to.
    * **markId** ID of the boat to map the device to.
* **deviceType**: Either `android` or `ios`. Needed for selecting correct push notification service.
* **deviceUuid**: **UUID** generated by the app on first run. **Not** an identifier that globally identifies the piece of hardware such UDID under iOS, IMEI, or other.
* **pushDeviceId** On Android the GCM regId, on iOS the APNS push token. This is needed for sending push notifications to the device.
* **fromMillis** Milliseconds timestamp from which to map the device to the competitor.

**Response:**
``HTTP 200``, no response body

**Additional Notes:**
For future versions, think about whether this is the right place to transmit the data for push notifications. Rather, this should ideally be done when installing the app (or starting it for the first time). Problem is, we don't know which server it will be used with then. So maybe, instead, whenever the user registers for a leaderboard on a _new_ server, the necessary information for push notifications ins transferrend _once_, instead of every time the user registers for a leaderboard on that server.

## Checkout

Ends the device to competitor coupling. Does not delete it, but rather marks the end timepoint. Fixes submitted for this device that have a timestamp which lies after the end timepoint specified in the checkout request are not added to races in the leaderboard.

**Path:** ``leaderboards/{leaderboard-name}/device_mappings/end``

**Verb:** ``POST``

**Request:**
```
{
    "competitorId" : "af855a56-9726-4a9c-a77e-da955bd289bg",
    "deviceUuid" : "af855a56-9726-4a9c-a77e-da955bd289bf",
    "toMillis" : 1415624564719
}
```
* **toMillis** Milliseconds timestamp up to which to map the device to the competitor.

**Response:**
``HTTP 200``, no response body


## Send Measurements (to the Fix Store)
<div id="fixes"></div>

The main data sent by the app.

Measurements should be sent live during an event. In case of missing network, measurements are stored to the device and uploaded in a FIFO order as soon as a connection is available.

GZIP compression is a must. Bulk uploads should be chunked, e.g. per 1,000 locations.

**Path:** ``gps_fixes``

**Verb:** ``POST``

**Request:**
```
{
  "deviceUuid" : "af855a56-9726-4a9c-a77e-da955bd289bf",
  "fixes" : [
    {
      "timestamp" : 14144160080000,
      "latitude" : 54.325246,
      "longitude" : 10.148556,
      "speed" : 3.61,
      "course" : 258.11,
    },
    {
      "timestamp" : 14144168490000,
      "latitude" : 55.12456,
      "longitude" : 8.03456,
      "speed" : 5.1,
      "course" : 14.2,
    }
  ]
}
```
* JSON array may contain one or several fixes
* **speed** Speed over ground in meters per second.
* **course** Bearing in degrees.


## Get Team Information of competitor (including team image)

Allows to retrieve team information for the team of a certain competitor

**Path:** ``competitors/{competitor-id}/team``

**Verb:** ``GET``

**Response:**
```
{  
   "name":"asd team",
   "coach":null,
   "sailors":[  
      {  
         "name":"asd",
         "description":null,
         "nationality":{  
            "IOC":"ALB"
         }
      }
   ],
   "imageUri":"http://images.forbes.com/media/lists/companies/google_200x200.jpg"
}
```

## Set Team Image

Set the team image of a certain competitor

**Path:** ``competitors/{competitor-id}/team/image``

**Verb:** ``POST``

Binary data (not multipart)

Headers:
- ``Content-Type:`` ``image/jpeg`` or ``image/png``
- ``Content-Length:`` set according to [HTTP spec](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13)

e.g.
```
$ curl -H "Content-Type:image/jpeg" --data-binary @<path-to-jpeg> \
   http://127.0.0.1:8888/sailingserver/api/v1/competitors/<competitor-id>/team/image
```

**Response:**
```
{
  "teamImageUri" : "http://training.sapsailing.com/team_images/9871d3a2c554b27151cacf1422eec048.jpeg"
}
```
## Leaderboard Integration

The leaderboard buttons loads the online leaderboard into an web-view. 

```
http://<host>/gwt/Leaderboard.html?name=<leaderboardName>&showRaceDetails=false&embedded=true&hideToolbar=true
```

# Buoy Tender (Tonnenleger) App
<div id="buoy-tender-app"></div>

An app aimed at buoy tenders. In v1, this only allows marks of a race to be _pinged_. The steps for doing so are

1. provide the information for which leaderboard and race to ping marks
2. get the marks for that race (potentially form the RaceLog, if no TrackedRace is yet present)
3. ping the marks by submitting fixes

## Check-In Information
<div id="bouy-tender-checkin-info"></div>

_see [bug 2650](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2650)_

In contrast to the _Tracking App_, we won't perform a check-in. However, we can deliver the information on leaderboard and race analogously through a URL (potentially QRCode), which is why this section is also called _Check-In Information_.

This information is represented in a URL with the following structure:
```
http://<host>/buoy-tender/checkin?event_id=<event-id>&leaderboard_name=<leaderboard-name>
```

_also see [Tracking App Check-In Information](#tracking-checkin-info) for additional notes that might apply_

## Get Marks

_see [bug 2651](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2651)_

Do not use the course to get the list of marks to ping. Instead, use the ``RaceLogDefineMarkEvent``s in the RaceLog, or ``TrackedRace#getMarks()``. As the JavaDoc of the latter states, not all marks for a race are necessarily present in the course - e.g. if they are backup buoys to be used in the case of a wind shift.

**Path:** ``leaderboards/{leaderboard-name}/marks?race_column={race-column-name}&fleet={fleet-name}``

The parameters ``race_column`` and ``fleet`` are optional. 

* neither ``race_column`` nor ``fleet``: all marks of the leaderboard get returned
* only ``race_column``:  all marks of the leaderboard's raceColumn get returned
* both ``race_column`` and ``fleet``: all marks of a certain fleet of a leaderboard's raceColumn
* only ``fleet``: HTTP/400 Bad Request 

Specifying an invalid leaderboard/race_column/fleet leads to an HTTP/404 Not found

**Verb:** ``GET``

**Response:**
```
[
  {
    "@class": "Mark",
    "name": "Start (1)",
    "id": "Start (1)",
    "type": "BUOY"
  },
  {
    "@class": "Mark",
    "name": "Start (2)",
    "id": "Start (2)",
    "type": "BUOY"
  },
  {
    "@class": "Mark",
    "name": "Windward",
    "id": "22a53380-046e-0132-0da7-60a44ce903c3",
    "type": "BUOY"
  }
]
```

## Ping Marks

_see [bug 2652](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2652)_

Instead of the checkin/checkout process of the tracking app, marks can be pinged using an API that hides the inner workings on the server completely. The server creates a device-mapping in the regatta-log for the exact timepoint of every fix, and adds the fixes to the GPSFixStore.

**Path:** ``leaderboards/{leaderboard-name}/marks/{mark-id}/gps_fixes``

**Verb:** ``POST``

**Request:**
```
{
      "timestamp" : 14144160080000,
      "latitude" : 54.325246,
      "longitude" : 10.148556,
}
```

**Response:**
If there is no existent GPS Fix for the Mark HTTP/200 will be returned.
If there is an existent GPS Fix the latest known Position of the Mark will be returned:
```
{
      "timestamp" : 14144160080000,
      "latitude" : 54.325246,
      "longitude" : 10.148556,
}
```

**Additional Notes:**
* the path for this endpoint intentionally has the leaderboard as a parent item
  * each mark should have a unique ID (``SharedDomainFactory#getOrCreateMark(String toStringRepresentationOfID, String name)``)
  * however, the mark cannot be pinged without the leaderboard as context, however, as the GPSFixStore contains only fixes with a device ID, and the mapping from that device to a mark has to be established through a RegattaLog (or RaceLog), which is attached to a (Flexible|Regatta)Leaderboard
  * an alternative endpoint definition might be ``POST marks/{mid}/gps_fixes?leaderboard_name={ln}``
