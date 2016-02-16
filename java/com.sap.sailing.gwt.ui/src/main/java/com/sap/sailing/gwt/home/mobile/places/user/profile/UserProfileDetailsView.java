package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.partials.userdetails.UserDetailsView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface UserProfileDetailsView extends IsWidget, NeedsAuthenticationContext {
    
    UserDetailsView getUserDetailsView();

    public interface Presenter extends NotLoggedInPresenter {
    }
}

