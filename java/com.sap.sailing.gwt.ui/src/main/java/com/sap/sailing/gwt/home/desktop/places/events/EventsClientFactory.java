package com.sap.sailing.gwt.home.desktop.places.events;

import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface EventsClientFactory extends SailingClientFactory, ClientFactoryWithDispatch {
    EventsView createEventsView();
}
