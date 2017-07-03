package com.sap.sailing.server.util;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.common.Util.Pair;

public interface LeaderboardUtil {
    
    public static int calculateRaceCount(Leaderboard sl) {
        return calculateRaces(sl).size();
    }
    
    public static Set<Pair<RaceColumn, Fleet>> calculateRaces(Leaderboard sl) {
        final Set<Pair<RaceColumn, Fleet>> result = new HashSet<>();
        for (RaceColumn column : sl.getRaceColumns()) {
            if (!column.isCarryForward()) {
                column.getFleets().forEach(fleet -> result.add(new Pair<>(column, fleet)));
            }
        }
        return result;
    }
}
