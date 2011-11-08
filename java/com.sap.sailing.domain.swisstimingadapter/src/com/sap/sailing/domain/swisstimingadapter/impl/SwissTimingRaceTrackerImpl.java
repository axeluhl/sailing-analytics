package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.swisstimingadapter.SailMasterConnector;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingRaceTracker;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.util.Util.Triple;

public class SwissTimingRaceTrackerImpl implements SwissTimingRaceTracker {
    private final SailMasterConnector connector;
    private final Set<RaceDefinition> races;
    
    protected SwissTimingRaceTrackerImpl(String raceID, String hostname, int port, SwissTimingFactory factory) {
        connector = factory.createSailMasterConnector(hostname, port);
        races = new HashSet<RaceDefinition>();
    }

    @Override
    public void stop() throws MalformedURLException, IOException, InterruptedException {
        connector.stop();
    }

    @Override
    public Set<RaceDefinition> getRaces() {
//        races = connector.getRaces();
        
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
