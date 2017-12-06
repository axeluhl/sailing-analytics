package com.sap.sailing.gwt.home.mobile.places.user.profile;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.user.profile.details.UserProfileDetailsActivity;
import com.sap.sailing.gwt.home.mobile.places.user.profile.preferences.UserProfilePreferencesActivity;
import com.sap.sailing.gwt.home.mobile.places.user.profile.settings.UserProfileSettingsActivity;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserProfilePreferencesPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserProfileSettingsPlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class UserProfileActivityProxy extends AbstractActivityProxy {

    private final MobileApplicationClientFactory clientFactory;
    private final AbstractUserProfilePlace currentPlace;

    public UserProfileActivityProxy(AbstractUserProfilePlace place, MobileApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (currentPlace instanceof UserProfilePreferencesPlace) {
                    UserProfilePreferencesPlace userProfilePrefsPlace = (UserProfilePreferencesPlace) currentPlace;
                    withFlagImageResolver(flagImageResolver -> new UserProfilePreferencesActivity(userProfilePrefsPlace, clientFactory, flagImageResolver));
                } else if(currentPlace instanceof UserProfileSettingsPlace) {
                    super.onSuccess(new UserProfileSettingsActivity((UserProfileSettingsPlace) currentPlace, clientFactory));
                } else {
                    super.onSuccess(new UserProfileDetailsActivity(currentPlace, clientFactory));
                }
            }
            private void withFlagImageResolver(final Function<FlagImageResolver, Activity> activityFactory) {
                final Consumer<Activity> onSuccess = super::onSuccess;
                final Consumer<Throwable> onFailure = super::onFailure;
                FlagImageResolver.get(new AsyncCallback<FlagImageResolver>() {
                    @Override
                    public void onSuccess(FlagImageResolver result) {
                        onSuccess.accept(activityFactory.apply(result));
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        onFailure.accept(caught);
                    }
                });
            }
        });
    }
}
