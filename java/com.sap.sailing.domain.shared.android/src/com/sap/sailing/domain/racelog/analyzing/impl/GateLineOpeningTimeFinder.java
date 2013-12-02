package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent.GateLineOpeningTimes;

public class GateLineOpeningTimeFinder extends RaceLogAnalyzer<GateLineOpeningTimes> {

    public GateLineOpeningTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected GateLineOpeningTimes performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogGateLineOpeningTimeEvent) {
                return ((RaceLogGateLineOpeningTimeEvent) event).getGateLineOpeningTimes();

            }
        }

        return null;
    }

}
