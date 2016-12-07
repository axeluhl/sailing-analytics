package com.sap.sailing.gwt.home.desktop.places.events;

import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface EventsClientFactory extends SailingClientFactory, ErrorAndBusyClientFactory, ClientFactoryWithDispatch {
    EventsView createEventsView();
}
