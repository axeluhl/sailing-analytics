package com.sap.sailing.domain.regattalike;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.HasRaceColumnsAndRegattaLike;
import com.sap.sailing.domain.leaderboard.Leaderboard;

public interface LeaderboardThatHasRegattaLike extends Leaderboard, HasRaceColumnsAndRegattaLike {
    Iterable<Competitor> getCompetitorsRegisteredInRegattaLog();
    
    Iterable<Boat> getBoatsRegisteredInRegattaLog();
}
