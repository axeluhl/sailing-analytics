package com.sap.sailing.server.impl;

import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.server.operationaltransformation.AddWaypoint;
import com.sap.sailing.server.operationaltransformation.RemoveWaypoint;

public class CourseChangeReplicator implements CourseListener {
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final RacingEventServiceImpl replicator;
    
    public CourseChangeReplicator(RacingEventServiceImpl replicator, Regatta regatta, RaceDefinition raceDefinition) {
        raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), raceDefinition.getName());
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
