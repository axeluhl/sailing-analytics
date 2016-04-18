package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsView;

public interface UserProfilePreferencesView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    UserDetailsView getUserDetailsView();
    
    NeedsAuthenticationContext getDecorator();

    public interface Presenter extends NotLoggedInPresenter, NeedsAuthenticationContext {
    }
}