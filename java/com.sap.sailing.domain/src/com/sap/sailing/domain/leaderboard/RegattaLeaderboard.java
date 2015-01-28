package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.regattalike.HasRegattaLike;

public interface RegattaLeaderboard extends Leaderboard, Renamable, HasRegattaLike {
    Regatta getRegatta();
}
