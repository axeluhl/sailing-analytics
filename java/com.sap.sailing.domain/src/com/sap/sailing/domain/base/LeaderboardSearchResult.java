package com.sap.sailing.domain.base;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.common.search.Hit;

public interface LeaderboardSearchResult extends Hit {
    Regatta getRegatta();
    Leaderboard getLeaderboard();
    EventBase getEvent();
}
