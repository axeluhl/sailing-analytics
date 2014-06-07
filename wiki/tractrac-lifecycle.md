# TracTrac Information

## Lifecycle

There is a lifecycle that every event manager should apply when working with the TracTrac console. It is VERY important that these rules are implemented during an event. One simple rule: After each day of racing set all races that has not been used to state HIDDEN!

- When races are created they are by default given the status 'OFFLINE'
 
- When a race is ready to open for the public to follow in live the status should be changed to 'ONLINE'

- When a race is over (tracking is ended), data should be transformed to a replay file , and status is changed to 'REPLAY'

- If a race for some reason is not to be visible to the public it should be given status 'HIDDEN'

All of the above management can be carried out from within the TracTrac Event Manager web-interface under the 'Races'-menu. It is the responsibility of the event tracking manager to carry out this house keeping.

With regard to 4) 'HIDDEN' there can be many different reasons to hide a race,
- it is planned but never raced (or cancelled/abondoned)
- the quality of the tracking data is to poor. (To many trackers depleted from battery, incorrect course definition leading to incomplete/incorrect mark rounding tables, etc

## Setting up course and time update URL

Our system is capable of sending course and time updates related to races directly to the TracTrac server. Whenever the race committee app sends out such an event it is not only stored in our system but also can be transmitted to TracTrac.

The information is as follows:

- Whenever you're working with the local setup (e.g. Extreme) then use http://&lt;local-ip&gt;:81/ and the user `trac@sapsailing.com` with password `sap0912`.

- For most use cases you can use the url http://secondary.traclive.dk:82/ (note the different port!) with the same auth as for the local case.

Internally the services will submit a JSON document that contains the relevant information by using the following urls:

- TracTracCourseDesignUpdateHandler: `http://URL/update_course?eventid=%s&raceid=%s&username=%s&password=%s`
- TracTracStartTimeResetHandler: `http://URL/resetStartTime?eventid=%s&raceid=%s&username=%s&password=%s`
- TracTracStartTimeUpdateHandler:  `http://URL/update_race_start_time?race_start_time=%s&eventid=%s&raceid=%s&username=%s&password=%s`