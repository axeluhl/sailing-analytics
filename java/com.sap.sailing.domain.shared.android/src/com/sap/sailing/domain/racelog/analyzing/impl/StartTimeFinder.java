package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sse.common.TimePoint;

public class StartTimeFinder extends RaceLogAnalyzer<TimePoint> {

    public StartTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected TimePoint performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogStartTimeEvent) {
                return ((RaceLogStartTimeEvent) event).getStartTime();
            }
        }
        
        return null;
    }

}
