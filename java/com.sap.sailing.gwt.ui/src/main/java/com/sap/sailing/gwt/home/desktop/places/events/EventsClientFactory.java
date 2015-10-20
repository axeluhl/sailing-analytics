package com.sap.sailing.gwt.home.desktop.places.events;

import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.client.refresh.ClientFactoryWithDispatch;

public interface EventsClientFactory extends SailingClientFactory, ClientFactoryWithDispatch {
    EventsView createEventsView();
}
