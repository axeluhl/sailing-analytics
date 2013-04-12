package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;

public class RaceStatusAnalyzer extends RaceLogAnalyzer {

    public RaceStatusAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    public RaceLogRaceStatus getStatus() {

        RaceLogRaceStatus newStatus = RaceLogRaceStatus.UNSCHEDULED;
        
        this.raceLog.lockForRead();
        try {
            newStatus = searchForRaceStatus();
        } finally {
            this.raceLog.unlockAfterRead();
        }
        
        return newStatus;
    }

    private RaceLogRaceStatus searchForRaceStatus() {
        RaceLogRaceStatus newStatus = RaceLogRaceStatus.UNSCHEDULED;
        
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogRaceStatusEvent) {
                RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
                newStatus = statusEvent.getNextStatus();
            }
        }
        
        return newStatus;
    }

}
