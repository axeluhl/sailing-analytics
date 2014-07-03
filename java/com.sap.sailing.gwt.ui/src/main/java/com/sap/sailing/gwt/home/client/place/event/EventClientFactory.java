package com.sap.sailing.gwt.home.client.place.event;

import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.player.Timer;

public interface EventClientFactory extends SailingClientFactory {
    EventView createEventView(EventDTO event, Timer timerForClientServerOffset);
}
