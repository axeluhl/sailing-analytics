package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;

public class ProtestStartTimeFinder extends RaceLogAnalyzer {

    public ProtestStartTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }
    
    public TimePoint getProtestStartTime() {
        this.raceLog.lockForRead();
        try {
            return searchForStartTime();
        } finally {
            this.raceLog.unlockAfterRead();
        }
    }

    private TimePoint searchForStartTime() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogProtestStartTimeEvent) {
                return ((RaceLogProtestStartTimeEvent) event).getProtestStartTime();
            }
        }
        return null;
    }

}
