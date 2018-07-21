package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface UserProfilePreferencesView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    NeedsAuthenticationContext getDecorator();
    
    public interface Presenter extends NotLoggedInPresenter, NeedsAuthenticationContext {
        UserPreferencesView.Presenter getUserPreferencesPresenter();
    }
}