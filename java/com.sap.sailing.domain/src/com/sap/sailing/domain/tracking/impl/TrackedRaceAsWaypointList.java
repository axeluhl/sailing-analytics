package com.sap.sailing.domain.tracking.impl;

import java.util.AbstractList;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.Util;

public class TrackedRaceAsWaypointList extends AbstractList<Waypoint> {
    private final TrackedRaceImpl trackedRace;

    protected TrackedRaceAsWaypointList(TrackedRaceImpl trackedRace) {
        this.trackedRace = trackedRace;
    }

    @Override
    public Waypoint get(int index) {
        final Waypoint result;
        final int numberOfLegs = Util.size(trackedRace.getTrackedLegs());
        if (index > numberOfLegs) {
            result = null;
        } else if (index == numberOfLegs) {
            result = Util.get(trackedRace.getTrackedLegs(), numberOfLegs-1).getLeg().getTo();
        } else {
            result = Util.get(trackedRace.getTrackedLegs(), index).getLeg().getFrom();
        }
        return result;
    }

    @Override
    public int size() {
        return Util.size(trackedRace.getTrackedLegs())+1;
    }

    @Override
    public void add(int index, Waypoint element) {
        trackedRace.waypointAdded(index, element);
    }

    @Override
    public Waypoint remove(int zeroBasedIndex) {
        final Waypoint waypointThatGotRemoved = get(zeroBasedIndex);
        if (waypointThatGotRemoved != null) {
            trackedRace.waypointRemoved(zeroBasedIndex, waypointThatGotRemoved);
        }
        return waypointThatGotRemoved;
    }

}
