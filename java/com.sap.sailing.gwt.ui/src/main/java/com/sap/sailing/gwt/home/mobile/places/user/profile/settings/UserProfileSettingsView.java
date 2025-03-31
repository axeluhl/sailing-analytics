package com.sap.sailing.gwt.home.mobile.places.user.profile.settings;

import com.sap.sailing.gwt.home.mobile.places.user.profile.UserProfileViewBase;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;

public interface UserProfileSettingsView extends UserProfileViewBase {
    
    public interface Presenter extends UserProfileViewBase.Presenter {
        UserSettingsView.Presenter getUserSettingsPresenter();
    }
}

