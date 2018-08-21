package com.sap.sailing.gwt.home.desktop.places.user.profile;

import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.ClientFactoryWithDispatchAndError;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.security.ui.authentication.WithAuthenticationManager;
import com.sap.sse.security.ui.authentication.WithUserService;

public interface UserProfileClientFactory extends ClientFactoryWithDispatch, SailingClientFactory,
        ErrorAndBusyClientFactory, WithAuthenticationManager, WithUserService, ClientFactoryWithDispatchAndError {
}
