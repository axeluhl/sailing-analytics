package com.sap.sailing.gwt.home.desktop.places.start;

import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface StartClientFactory extends SailingClientFactory, ErrorAndBusyClientFactory, ClientFactoryWithDispatch {
    StartView createStartView();
}
