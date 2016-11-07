package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;

public class ProtestStartTimeFinder extends RaceLogAnalyzer<RaceLogProtestStartTimeEvent> {

    public ProtestStartTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RaceLogProtestStartTimeEvent performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogProtestStartTimeEvent) {
                return ((RaceLogProtestStartTimeEvent) event);
            }
        }
        return null;
    }

}
