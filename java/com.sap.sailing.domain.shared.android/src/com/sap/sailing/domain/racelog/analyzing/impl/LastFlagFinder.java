package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public class LastFlagFinder extends RaceLogAnalyzer {

    public LastFlagFinder(RaceLog raceLog) {
        super(raceLog);
    }

    public RaceLogFlagEvent getLastFlagEvent() {
        RaceLogFlagEvent lastFlagEvent = null;
        
        this.raceLog.lockForRead();
        try {
            lastFlagEvent = searchForLastFlagEvent();
        } finally {
            this.raceLog.unlockAfterRead();
        }

        return lastFlagEvent;
    }

    private RaceLogFlagEvent searchForLastFlagEvent() {
        RaceLogFlagEvent lastFlagEvent = null;
        
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogFlagEvent) {
                lastFlagEvent = (RaceLogFlagEvent) event;
            }
        }
        
        return lastFlagEvent;
    }

}
