package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sse.common.TimePoint;

public class RaceLogGPSFixTracker extends AbstractRaceLogFixTracker {
    private static final Logger logger = Logger.getLogger(RaceLogGPSFixTracker.class.getName());
    
    private final GPSFixStore gpsFixStore;
    private final RegattaLogEventVisitor regattaLogEventVisitor = new BaseRegattaLogEventVisitor() {

        @Override
        public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
            logger.log(Level.FINE, "New mapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            // TODO updateMappingsAndAddListeners();
        }

        @Override
        public void visit(RegattaLogDeviceMarkMappingEvent event) {
            logger.log(Level.FINE, "New mapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            // TODO updateMappingsAndAddListeners();
        }
        
        @Override
        public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
            logger.log(Level.FINE, "Mapping closed: " + event.getDeviceMappingEventId());
            updateMappingsAndAddListeners();
        }
        
        @Override
        public void visit(RegattaLogRevokeEvent event) {
            logger.log(Level.FINE, "Mapping revoked for: " + event.getRevokedEventId());
            updateMappingsAndAddListeners();
        };
    };
    
    public RaceLogGPSFixTracker(DynamicTrackedRegatta regatta, DynamicTrackedRace trackedRace, GPSFixStore gpsFixStore) {
        super(regatta, trackedRace);
        this.gpsFixStore = gpsFixStore;
        startTracking();
    }

    @Override
    protected void loadFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected RegattaLogEventVisitor getRegattaLogEventVisitor() {
        return regattaLogEventVisitor;
    }

    @Override
    protected void updateMappingsAndAddListenersImpl() {
        // TODO Auto-generated method stub
        
    }
}
