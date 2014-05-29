# TracTrac Race Lifecycle

There is a lifecycle that every event manager should apply when working with the TracTrac console. It is VERY important that these rules are implemented during an event. One simple rule: After each day of racing set all races that has not been used to state HIDDEN!

1) When races are created they are by default given the status 'OFFLINE'

2) When a race is ready to open for the public to follow in live the status should be changed to 'ONLINE'

3) When a race is over (tracking is ended), data should be transformed to a replay file , and status is changed to 'REPLAY'

4) If a race for some reason is not to be visible to the public it should be given status 'HIDDEN'

All of the above management can be carried out from within the TracTrac Event Manager web-interface under the 'Races'-menu. It is the responsibility of the event tracking manager to carry out this house keeping.

With regard to 4) 'HIDDEN' there can be many different reasons to hide a race,
- it is planned but never raced (or cancelled/abondoned)
- the quality of the tracking data is to poor. (To many trackers depleted from battery, incorrect course definition leading to incomplete/incorrect mark rounding tables, etc