package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface HasLeaderboardGroupContext {

    @SideEffectFreeValue(messageKey="LeaderboardGroup")
    public LeaderboardGroup getLeaderboardGroup();

}