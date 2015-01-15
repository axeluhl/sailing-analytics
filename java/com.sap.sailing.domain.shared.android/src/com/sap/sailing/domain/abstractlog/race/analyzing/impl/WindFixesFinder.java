package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.tracking.Wind;

public class WindFixesFinder extends RaceLogAnalyzer<List<Wind>> {

    public WindFixesFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected List<Wind> performAnalysis() {
        final List<Wind> windFixes = new ArrayList<Wind>();
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event instanceof RaceLogWindFixEvent) {
                RaceLogWindFixEvent windFixEvent = (RaceLogWindFixEvent) event;
                windFixes.add(windFixEvent.getWindFix());
            }
        }
        return windFixes;
    }

}
