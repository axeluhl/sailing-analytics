package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sse.common.TimePoint;

public class RaceLogGPSFixTracker extends AbstractRaceLogFixTracker {
    private final GPSFixStore gpsFixStore;
    
    public RaceLogGPSFixTracker(DynamicTrackedRegatta regatta, DynamicTrackedRace trackedRace, GPSFixStore gpsFixStore) {
        super(regatta, trackedRace);
        this.gpsFixStore = gpsFixStore;
    }

    @Override
    protected void loadFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected RegattaLogEventVisitor getRegattaLogEventVisitor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void updateMappingsAndAddListenersImpl() {
        // TODO Auto-generated method stub
        
    }
}
