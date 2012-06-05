package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Regatta;

public interface RegattaLeaderboard extends Leaderboard {
    Regatta getRegatta();
}
