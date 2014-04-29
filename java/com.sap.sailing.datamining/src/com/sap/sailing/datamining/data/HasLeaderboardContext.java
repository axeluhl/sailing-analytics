package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface HasLeaderboardContext extends HasLeaderboardGroupContext {

    @SideEffectFreeValue(messageKey="Leaderboard")
    public Leaderboard getLeaderboard();

}