package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class RaceColumnRaceLogReplicator implements RaceLogEventVisitor {
    
    private final RaceColumn raceColumn;
    private final RaceLogIdentifier identifier;
    
    public RaceColumnRaceLogReplicator(RaceColumn raceColumn, RaceLogIdentifier identifier) {
        this.raceColumn = raceColumn;
        this.identifier = identifier;
    }
        
    private void notifyOnAdd(RaceLogEvent event) {
        raceColumn.getRaceColumnListeners().notifyListenersAboutRaceLogEventAdded(
                raceColumn, 
                identifier, 
                event);
    }
    
    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(RaceLogStartTimeEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(RaceLogRaceStatusEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(RaceLogPassChangeEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(RaceLogFlagEvent event) {
        notifyOnAdd(event);
    }
}
