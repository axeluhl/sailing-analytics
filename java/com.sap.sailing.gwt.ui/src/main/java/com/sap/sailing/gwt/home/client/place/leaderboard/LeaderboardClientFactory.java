package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface LeaderboardClientFactory extends SailingClientFactory {
    LeaderboardView createLeaderboardView(LeaderboardActivity activity);
}
