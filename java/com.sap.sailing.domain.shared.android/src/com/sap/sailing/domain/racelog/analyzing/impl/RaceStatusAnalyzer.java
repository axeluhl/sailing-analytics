package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;

public class RaceStatusAnalyzer extends RaceLogAnalyzer<RaceLogRaceStatus> {

    public RaceStatusAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RaceLogRaceStatus performAnalyzation() {
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
