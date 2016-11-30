package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sse.common.TimeRange;

public class ProtestTimeFinder extends RaceLogAnalyzer<TimeRange> {

    public ProtestTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected TimeRange performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogProtestStartTimeEvent) {
                RaceLogProtestStartTimeEvent protestEvent = (RaceLogProtestStartTimeEvent) event;
                return protestEvent.getProtestTime();
            }
        }
        return null;
    }

}
