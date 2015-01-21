package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent.GateLineOpeningTimes;

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
