package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;

public class WindFixesFinder extends RaceLogAnalyzer<List<RaceLogWindFixEvent>> {

    public WindFixesFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected List<RaceLogWindFixEvent> performAnalysis() {
        final List<RaceLogWindFixEvent> windFixes = new ArrayList<>();
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event instanceof RaceLogWindFixEvent) {
                RaceLogWindFixEvent windFixEvent = (RaceLogWindFixEvent) event;
                windFixes.add(windFixEvent);
            }
        }
        return windFixes;
    }
}
