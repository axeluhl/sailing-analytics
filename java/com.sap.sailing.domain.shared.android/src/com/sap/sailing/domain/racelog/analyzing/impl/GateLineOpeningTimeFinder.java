package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;

public class GateLineOpeningTimeFinder extends RaceLogAnalyzer {

    public GateLineOpeningTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    public Long getGateLineOpeningTime() {
        Long gateLineOpeningTime;
        this.raceLog.lockForRead();
        try {
            gateLineOpeningTime = searchForGateLineOpeningTime();
        } finally {
            this.raceLog.unlockAfterRead();
        }

        return gateLineOpeningTime;
    }

    private Long searchForGateLineOpeningTime() {
        Long gateLineOpeningTime = null;

        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogGateLineOpeningTimeEvent) {
                gateLineOpeningTime = ((RaceLogGateLineOpeningTimeEvent) event).getGateLineOpeningTime();

            }
        }

        return gateLineOpeningTime;
    }

}
