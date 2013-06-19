package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class StartTimeFinder extends RaceLogAnalyzer<TimePoint> {

    public StartTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected TimePoint performAnalyzation() {
        TimePoint startTime = null;
        
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogStartTimeEvent) {
                startTime = ((RaceLogStartTimeEvent) event).getStartTime();
            }
        }
        
        return startTime;
    }

}
