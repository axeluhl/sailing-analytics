package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.common.Wind;

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
