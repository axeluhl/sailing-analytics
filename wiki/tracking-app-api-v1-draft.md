# Tracking App API v1 Draft

## General

This is a design for the REST API to be used by the iOS and Android tracking apps.
We have decided to handle checkins etc. at the level of ``Leaderboard``s instead of ``Regatta``s. For most cases, such a Leaderboard is based on a Regatta (a ``RegattaLeaderboard`` in our domain model).

This document supplements the [documentation of the existing REST API](http://www.sapsailing.com/sailingserver/webservices/api/v1/index.html), highlighting those resources necessary for the tracking app, and specifying such resources that are not yet present in the API.

### URL base
```
http://<host>/sailingserver/api/v1/
```
All relative URLs given below are relative to this base URL.

## Push Notifications

Event data can be updated via push notifications. These are limited on iOS to 256 bytes (not characters). For this reason, on receiving a push notification, the app must GET the event data.


## Checkin Information

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
* ``competitor-id`` (so the app can register the device for the correct competitor)

This information is represented in a URL with the following structure:
```
http://<host>/tracking/checkin
  ?event_id=<event-id>
  &leaderboard_name=<leaderboard-name>
  &competitor_id=<competitor-id>
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

### Competitor Information

**Path:** ``competitors/{competitor-id}``

**Verb:** ``GET``

**Response:**
```
{
  "name": "Heiko KRÖGER",
  "displayName": "Heiko KRÖGER",
  "id": "af855a56-9726-4a9c-a77e-da955bd289be",
  "sailID": "GER 1",
  "nationality": "GER",
  "countryCode": "DE"
}
```

**Additional Notes:**
* The semantics of ``displayName`` are explained in the JavaDoc of ``Leaderboard#getDisplayName()``.
* Competitor profile image left out for now.
* No flag image included. These images are present on the server in the GWT-context only as 16x11 pngs currently. How flags are displayed in the app should probably be independent. I would suggest resolving the appropriate image within the app based on the ``countryCode``.


## Check-In

After the user has received the event data with the short URL, the smartphone is mapped to a competitor.

Result: fixes transmitted after the time specified in the checkin request are added to such races within the leaderboard that have a start/end time matching the fixes timestamp.

_Question: What should be done if a second device is mapped to competitor?_
* _Answer:_ For now, according to our internal logic, both devices are actually mapped. If they send fixes for the same timerange, we will see some weird behaviour.

**Path:** ``/leaderboards/{leaderboard-name}/device_mappings/start``

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
* **competitorId** ID of the competitor to map the device to. For coming versions, using ``markId`` instead might also be valid to map the device to a mark.
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

**Path:** ``/leaderboards/{leaderboard-name}/device_mappings/end``

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

The main data sent by the app.

Measurements should be sent live during an event. In case of missing network, measurements are stored to the device and uploaded in a FIFO order as soon as a connection is available.

GZIP compression is a must. Bulk uploads should be chunked, e.g. per 1,000 locations.

**Path:** ``gps_fixes``

**Verb:** ``POST``

**Request:**
```
{
  "deviceUuid": "af855a56-9726-4a9c-a77e-da955bd289bf",
  "fixes": [
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