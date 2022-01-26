package com.sap.sailing.gwt.managementconsole.services.factories;

import com.sap.sailing.gwt.ui.adminconsole.LeaderboardGroupDialog.LeaderboardGroupDescriptor;

public abstract class LeaderboardGroupFactory {

    private static final String DEFAULT_LEADERBOARD_GROUP_NAME = "Leaderboard";

    public static LeaderboardGroupDescriptor createDefaultLeaderboardGroupDescriptor() {
        final String leaderboardName = DEFAULT_LEADERBOARD_GROUP_NAME + System.currentTimeMillis()
                + (Math.random() * 10000);
        final LeaderboardGroupDescriptor leaderboardGroupDescriptor = new LeaderboardGroupDescriptor(leaderboardName,
                leaderboardName, leaderboardName, false, false, new int[0], null);
        return leaderboardGroupDescriptor;
    }

}
