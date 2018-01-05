package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.regattalike.LeaderboardThatHasRegattaLike;
import com.sap.sse.common.Renamable;

public interface RegattaLeaderboard extends Renamable, LeaderboardThatHasRegattaLike {
    Regatta getRegatta();
}
