package com.sap.sailing.racecommittee.domain.state.impl.analyzers;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;

public class FinishedTimeFinder extends RaceLogAnalyzer {

    public FinishedTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    public TimePoint getFinishedTime() {
        TimePoint finishedTime = null;
        
        this.raceLog.lockForRead();
        try {
            finishedTime = searchForFinishedTime();
        } finally {
            this.raceLog.unlockAfterRead();
        }

        return finishedTime;
    }
    
    private TimePoint searchForFinishedTime() {
        TimePoint finishedTime = null;
        
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogRaceStatusEvent) {
                RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
                if (statusEvent.getNextStatus().equals(RaceLogRaceStatus.FINISHED)) {
                    finishedTime = statusEvent.getTimePoint();
                }
            }
        }
        
        return finishedTime;
    }

}
