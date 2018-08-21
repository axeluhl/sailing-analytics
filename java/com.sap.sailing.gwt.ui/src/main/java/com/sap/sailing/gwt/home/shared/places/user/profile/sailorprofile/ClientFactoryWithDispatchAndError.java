package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.gwt.client.mvp.ClientFactory;

public interface ClientFactoryWithDispatchAndError
        extends ClientFactoryWithDispatch, ErrorAndBusyClientFactory, ClientFactory {

}
