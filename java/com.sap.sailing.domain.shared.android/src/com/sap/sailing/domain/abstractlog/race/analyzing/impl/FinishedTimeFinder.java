package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.TimePoint;

public class FinishedTimeFinder extends RaceLogAnalyzer<TimePoint> {

    public FinishedTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected TimePoint performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogRaceStatusEvent) {
                RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
                if (statusEvent.getNextStatus().equals(RaceLogRaceStatus.FINISHED)) {
                    return statusEvent.getLogicalTimePoint();
                }
            }
        }
        
        return null;
    }

}
