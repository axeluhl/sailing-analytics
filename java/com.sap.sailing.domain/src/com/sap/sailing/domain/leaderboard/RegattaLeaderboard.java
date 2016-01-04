package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.regattalike.LeaderboardThatHasRegattaLike;

public interface RegattaLeaderboard extends Renamable, LeaderboardThatHasRegattaLike {
    Regatta getRegatta();
}
