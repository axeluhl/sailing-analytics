package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;

public class PathfinderFinder extends RaceLogAnalyzer {

    public PathfinderFinder(RaceLog raceLog) {
        super(raceLog);
    }

    public String getPathfinderId() {
        String pathfinderId;
        this.raceLog.lockForRead();
        try {
            pathfinderId = searchForPathfinderId();
        } finally {
            this.raceLog.unlockAfterRead();
        }

        return pathfinderId;
    }

    private String searchForPathfinderId() {
        String pathfinderId = null;

        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogPathfinderEvent) {
                pathfinderId = ((RaceLogPathfinderEvent) event).getPathfinderId();

            }
        }

        return pathfinderId;
    }

}
