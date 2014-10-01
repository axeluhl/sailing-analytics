package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.player.Timer;

public interface LeaderboardClientFactory extends SailingClientFactory {
    AnalyticsView createLeaderboardView(EventDTO event, String leaderboardName, Timer timerForClientServerOffset);
}
