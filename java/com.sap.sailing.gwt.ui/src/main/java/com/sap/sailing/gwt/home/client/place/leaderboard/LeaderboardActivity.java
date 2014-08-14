package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public class LeaderboardActivity extends AbstractActivity {
    private final LeaderboardClientFactory clientFactory;
    private final LeaderboardPlace leaderboardPlace;
    private final Timer timerForClientServerOffset;

    public LeaderboardActivity(LeaderboardPlace place, LeaderboardClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.leaderboardPlace = place;
        
        timerForClientServerOffset = new Timer(PlayModes.Replay);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new Placeholder());

        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
        String leaderStringName = leaderboardPlace.getLeaderboardIdAsNameString();
        
        panel.setWidget(new TabletAndDesktopLeaderboardView());
    }
}
