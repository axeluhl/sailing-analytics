package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;

public class FinishingTimeFinder extends RaceLogAnalyzer<TimePoint> {

    public FinishingTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected TimePoint performAnalyzation() {
        TimePoint finishingTime = null;
        
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogRaceStatusEvent) {
                RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
                if (statusEvent.getNextStatus().equals(RaceLogRaceStatus.FINISHING)) {
                    finishingTime = statusEvent.getTimePoint();
                }
            }
        }
        
        return finishingTime;
    }

}
