package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.leaderboard.Leaderboard;

public interface HasLeaderboardContext extends HasLeaderboardGroupContext {

    public Leaderboard getLeaderboard();

}