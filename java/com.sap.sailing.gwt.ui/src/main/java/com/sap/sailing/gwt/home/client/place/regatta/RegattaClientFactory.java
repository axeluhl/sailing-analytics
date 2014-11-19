package com.sap.sailing.gwt.home.client.place.regatta;

import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace.RegattaNavigationTabs;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.player.Timer;

public interface RegattaClientFactory extends SailingClientFactory {
    RegattaAnalyticsView createRegattaAnalyticsView(EventDTO event, String leaderboardName, RegattaNavigationTabs navigationTab, Timer timerForClientServerOffset);
}
