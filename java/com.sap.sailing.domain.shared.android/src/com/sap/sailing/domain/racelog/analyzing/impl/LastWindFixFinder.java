package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.tracking.Wind;

public class LastWindFixFinder extends RaceLogAnalyzer<Wind> {

    public LastWindFixFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Wind performAnalysis() {
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event instanceof RaceLogWindFixEvent) {
                RaceLogWindFixEvent windFixEvent = (RaceLogWindFixEvent) event;
                return windFixEvent.getWindFix();
            }
        }
        
        return null;
    }

}
