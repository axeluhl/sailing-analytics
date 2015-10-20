package com.sap.sailing.gwt.home.desktop.places.event;

import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.client.refresh.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface EventClientFactory extends SailingClientFactory, ErrorAndBusyClientFactory, ClientFactoryWithDispatch {
}
