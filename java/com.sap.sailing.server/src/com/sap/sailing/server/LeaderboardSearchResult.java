package com.sap.sailing.server;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.common.search.Hit;

public interface LeaderboardSearchResult extends Hit {
    Regatta getRegatta();
    Leaderboard getLeaderboard();
}
