package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sse.common.TimePoint;

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
