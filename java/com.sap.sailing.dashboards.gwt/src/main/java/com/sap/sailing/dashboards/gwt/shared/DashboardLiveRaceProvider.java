package com.sap.sailing.dashboards.gwt.shared;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class DashboardLiveRaceProvider {
    
    private Map<String, TrackedRace> liveRaceForLeaderboardNames;
    private RacingEventService racingEventService;
    
    public DashboardLiveRaceProvider(RacingEventService racingEventService) {
        liveRaceForLeaderboardNames = new HashMap<String, TrackedRace>();
        this.racingEventService = racingEventService;
    }
    
    public void validateLiveRaceForLeaderboardName(String leaderboardName) {
        Leaderboard leaderboard = racingEventService.getLeaderboardByName(leaderboardName);
        TrackedRace liveRace = null;
        if (leaderboard != null) {
            for (RaceColumn column : leaderboard.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    TrackedRace race = column.getTrackedRace(fleet);
                    if (race != null && race.isLive(MillisecondsTimePoint.now())) {
                        liveRace = race;
                    }
                }
            }
        }
        if (liveRace != null)
            setLiveRaceForLeaderboardName(leaderboardName, liveRace);
    }
    
    public TrackedRace getLiveRaceForLeaderboardName(String leaderboardName) {
        return liveRaceForLeaderboardNames.get(leaderboardName);
    }
    
    private void setLiveRaceForLeaderboardName(String leaderboardName, TrackedRace liveRace) {
        if (liveRaceForLeaderboardNames.containsKey(leaderboardName)) {
            liveRaceForLeaderboardNames.remove(leaderboardName);
        }
        liveRaceForLeaderboardNames.put(leaderboardName, liveRace);
    }
}
