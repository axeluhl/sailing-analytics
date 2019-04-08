package com.sap.sailing.gwt.home.mobile.places.user.profile.preferences;

import com.sap.sailing.gwt.home.mobile.places.user.profile.UserProfileViewBase;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesView;

public interface UserProfilePreferencesView extends UserProfileViewBase {
    
    public interface Presenter extends UserProfileViewBase.Presenter {
        UserPreferencesView.Presenter getUserPreferencesPresenter();
    }
}

