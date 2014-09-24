package com.sap.sailing.gwt.autoplay.client.place.player;

import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface PlayerClientFactory extends SailingClientFactory {
    PlayerView createPlayerView(EventDTO event, String leaderboardName);
}
