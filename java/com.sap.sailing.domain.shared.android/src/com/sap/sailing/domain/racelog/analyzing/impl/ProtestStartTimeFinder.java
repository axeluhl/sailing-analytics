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
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogProtestStartTimeEvent) {
                TimePoint result = ((RaceLogProtestStartTimeEvent) event).getProtestStartTime();
                return result;
            }
        }
        return null;
    }

}
