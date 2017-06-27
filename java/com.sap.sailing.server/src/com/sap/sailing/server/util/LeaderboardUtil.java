package com.sap.sailing.server.util;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;

public interface LeaderboardUtil {
    
    public static int calculateRaceCount(Leaderboard sl) {
        int nonCarryForwardRacesCount = 0;
        for (RaceColumn column : sl.getRaceColumns()) {
            if (!column.isCarryForward()) {
                nonCarryForwardRacesCount += Util.size(column.getFleets());
            }
        }
        return nonCarryForwardRacesCount;
    }
    
    public static int calculateTrackedRaceCount(Leaderboard sl) {
        int count=0;
        for (RaceColumn column : sl.getRaceColumns()) {
            for (Fleet fleet : column.getFleets()) {
                TrackedRace trackedRace = column.getTrackedRace(fleet);
                if(trackedRace != null && trackedRace.hasGPSData() && trackedRace.hasWindData()) {
                    count++;
                }
            }
        }
        return count;
    }
}
