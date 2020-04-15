package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.sap.sailing.gwt.home.desktop.places.user.profile.WithSailingService;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.security.ui.authentication.WithUserService;

public interface ClientFactoryWithDispatchAndErrorAndUserService
        extends ClientFactoryWithDispatch, ErrorAndBusyClientFactory, WithUserService, ClientFactory,
        WithSailingService {

}
