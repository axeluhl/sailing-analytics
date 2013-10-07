package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;

public class ProtestStartTimeFinder extends RaceLogAnalyzer<TimePoint> {

    public ProtestStartTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected TimePoint performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogProtestStartTimeEvent) {
                return ((RaceLogProtestStartTimeEvent) event).getProtestStartTime();
            }
        }
        return null;
    }

}
