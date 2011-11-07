package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingRaceTracker;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.util.Util.Triple;

public class SwissTimingRaceTrackerImpl implements SwissTimingRaceTracker {
    protected SwissTimingRaceTrackerImpl(String raceID, String hostname, int port) {
        // TODO implement SwissTimingRaceTrackerImpl constructor
    }

    @Override
    public void stop() throws MalformedURLException, IOException, InterruptedException {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<RaceDefinition> getRaces() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RaceHandle getRaceHandle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindStore getWindStore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Event getEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Triple<String, String, Integer> getID() {
        // TODO Auto-generated method stub
        return null;
    }

}
