package com.sap.sailing.gwt.home.client.place.event;

import java.util.List;

import com.sap.sailing.gwt.home.client.place.event.EventPlace.EventNavigationTabs;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sse.gwt.client.player.Timer;

public interface EventClientFactory extends SailingClientFactory {
    EventView createEventView(EventDTO event, EventNavigationTabs navigationTab, List<RaceGroupDTO> raceGroups, String leaderboardName, Timer timerForClientServerOffset);

    EventWithoutRegattasView createEventWithoutRegattasView(EventDTO event);
}
