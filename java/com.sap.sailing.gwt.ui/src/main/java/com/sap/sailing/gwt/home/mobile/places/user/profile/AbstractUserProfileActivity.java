package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.activity.shared.AbstractActivity;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;

public abstract class AbstractUserProfileActivity extends AbstractActivity implements UserProfileViewBase.Presenter{

    protected final MobileApplicationClientFactory clientFactory;

    protected AbstractUserProfileActivity(MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
    
    @Override
    public void doTriggerLoginForm() {
        clientFactory.getNavigator().getSignInNavigation().goToPlace();
    }
    
    @Override
    public PlaceNavigation<? extends AbstractUserProfilePlace> getUserProfileNavigation() {
        return clientFactory.getNavigator().getUserProfileNavigation();
    }
    
    @Override
    public PlaceNavigation<? extends AbstractUserProfilePlace> getUserPreferencesNavigation() {
        return clientFactory.getNavigator().getUserPreferencesNavigation();
    }
    
    @Override
    public PlaceNavigation<? extends AbstractUserProfilePlace> getUserSettingsNavigation() {
        return clientFactory.getNavigator().getUserSettingsNavigation();
    }
    
}
