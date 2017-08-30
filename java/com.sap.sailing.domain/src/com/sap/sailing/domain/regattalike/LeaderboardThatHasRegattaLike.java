package com.sap.sailing.domain.regattalike;

import java.util.Map;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.HasRaceColumnsAndRegattaLike;
import com.sap.sailing.domain.leaderboard.Leaderboard;

public interface LeaderboardThatHasRegattaLike extends Leaderboard, HasRaceColumnsAndRegattaLike {
    Map<Competitor, Boat> getCompetitorsAndBoatsRegisteredInRegattaLog();
}
