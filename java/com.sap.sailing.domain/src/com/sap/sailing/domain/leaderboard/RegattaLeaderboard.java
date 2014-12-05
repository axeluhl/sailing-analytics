package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Renamable;

public interface RegattaLeaderboard extends Leaderboard, Renamable, HasRegattaLog {
    Regatta getRegatta();
}
