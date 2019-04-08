package com.sap.sailing.gwt.home.desktop.places.user.profile;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.app.WithHeader;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ProvidesNavigationPath;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.UserProfileDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.UserProfileDetailsPlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class UserProfileActivityProxy extends AbstractActivityProxy implements ProvidesNavigationPath, WithHeader {

    private AbstractUserProfilePlace place;
    private UserProfileClientFactory clientFactory;
    private final DesktopPlacesNavigator homePlacesNavigator;
    private NavigationPathDisplay navigationPathDisplay;

    public UserProfileActivityProxy(AbstractUserProfilePlace place, UserProfileClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
    }
    
    @Override
    public void setNavigationPathDisplay(NavigationPathDisplay navigationPathDisplay) {
        this.navigationPathDisplay = navigationPathDisplay;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if(place instanceof UserProfileDefaultPlace) {
                    place = new UserProfileDetailsPlace();
                }
                withFlagImageResolver(flagImageResolver -> new UserProfileActivity(place, clientFactory,
                        homePlacesNavigator, navigationPathDisplay, flagImageResolver));
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
