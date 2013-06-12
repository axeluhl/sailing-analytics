package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public abstract class RaceLogAnalyzer {

    protected RaceLog raceLog;

    public RaceLogAnalyzer(RaceLog raceLog) {
        this.raceLog = raceLog;
    }

    protected Iterable<RaceLogEvent> getPassEvents() {
        return raceLog.getFixes();
    }

    protected Iterable<RaceLogEvent> getAllEvents() {
        return raceLog.getRawFixes();
    }
    
    protected Iterable<RaceLogEvent> getPassEventsDescending() {
        return raceLog.getFixesDescending();
    }
    
    protected Iterable<RaceLogEvent> getAllEventsDescending() {
        return raceLog.getRawFixesDescending();
    }
}
