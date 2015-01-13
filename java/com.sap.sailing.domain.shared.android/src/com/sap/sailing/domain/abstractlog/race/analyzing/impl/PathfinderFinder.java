package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;

public class PathfinderFinder extends RaceLogAnalyzer<String> {

    public PathfinderFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected String performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogPathfinderEvent) {
                return ((RaceLogPathfinderEvent) event).getPathfinderId();
            }
        }
        return null;
    }

}
