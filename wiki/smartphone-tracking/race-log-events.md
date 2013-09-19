# RaceLog Events
[[_TOC_]]

## Persistent Competitor Registered
`RaceLogPersistentCompetitorRegisteredEvent`

Includes a `Competitor` as well a `SmartphoneIdentifier`. On the one hand, every comptitor that is thus registered will be included in the `RaceDefinition` as soon as the race is created, on the other hand the mapping between smartphone identifier (e.g. IMEI for european phones) and competitor is later used for mapping the incoming fixes to the correct competitor.

## Pre Race Phase Started
`RaceLogPreRacePhaseStartedEvent`

This event is added to the race log automatically by the server when calling the [createRace](/wiki/smartphone-tracking/servlets#createRace) servlet, to mark this race as race-log-tracked. It also includes the race name and boat class, so that the race can be properly reloaded after a server restart.

## Pre Race Phase Ended
`RaceLogPreRacePhaseEndedEvent`

This does not include any additional data, and merely indicates that the race can be transformed from its pre-race definition state (e.g. waiting for competitors to register, waiting for boat class, waiting for course definition) to an actual race, where no additional competitors can be added, the boat class is fixes, and tracking may begin. This event is picked up by the `RaceLogRaceTracker`, which then creates the actual tracked race from the data in the RaceLog. For successful creation, at least one competitor has to be registered, and a course must have been set through a `RaceLogCourseDefinitionChangedEvent`.

## Course Design Changed
`RaceLogCourseDesignChangedEvent`

This existing event is used to set the course definition, and is required before the race can be started.

## Start Time
`RaceLogStartTimeEvent`

This existing event should be used to set the start time, before attempting to view the race in the race viewer, as otherwise the time slider won't work.