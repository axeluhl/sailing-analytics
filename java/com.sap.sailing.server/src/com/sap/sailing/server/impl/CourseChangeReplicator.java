package com.sap.sailing.server.impl;

import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.common.EventNameAndRaceName;
import com.sap.sailing.server.operationaltransformation.AddWaypoint;
import com.sap.sailing.server.operationaltransformation.RemoveWaypoint;

public class CourseChangeReplicator implements CourseListener {
    private final EventAndRaceIdentifier raceIdentifier;
    private final RacingEventServiceImpl replicator;
    
    public CourseChangeReplicator(RacingEventServiceImpl replicator, Event event, RaceDefinition raceDefinition) {
        raceIdentifier = new EventNameAndRaceName(event.getName(), raceDefinition.getName());
        this.replicator = replicator;
    }

    @Override
    public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
        AddWaypoint op = new AddWaypoint(raceIdentifier, zeroBasedIndex, waypointThatGotAdded);
        replicator.replicate(op);
    }

    @Override
    public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        RemoveWaypoint op = new RemoveWaypoint(raceIdentifier, zeroBasedIndex, waypointThatGotRemoved);
        replicator.replicate(op);
    }

}
