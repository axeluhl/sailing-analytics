package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;

public class PathfinderFinder extends RaceLogAnalyzer<String> {

    public PathfinderFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected String performAnalyzation() {
        String pathfinderId = null;
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogPathfinderEvent) {
                pathfinderId = ((RaceLogPathfinderEvent) event).getPathfinderId();
            }
        }
        return pathfinderId;
    }

}
