package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;

public class GateLineOpeningTimeFinder extends RaceLogAnalyzer<Long> {

    public GateLineOpeningTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Long performAnalyzation() {
        Long gateLineOpeningTime = null;

        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogGateLineOpeningTimeEvent) {
                gateLineOpeningTime = ((RaceLogGateLineOpeningTimeEvent) event).getGateLineOpeningTime();

            }
        }

        return gateLineOpeningTime;
    }

}
