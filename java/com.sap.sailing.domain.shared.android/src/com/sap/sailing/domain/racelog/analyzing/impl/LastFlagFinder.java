package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public class LastFlagFinder extends RaceLogAnalyzer<RaceLogFlagEvent> {

    public LastFlagFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RaceLogFlagEvent performAnalyzation() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogFlagEvent) {
                return (RaceLogFlagEvent) event;
            }
        }
        return null;
    }

}
