package com.sap.sailing.gwt.home.client.app.event;

import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface EventClientFactory extends SailingClientFactory {
    EventView createEventView(EventDTO event);
}
