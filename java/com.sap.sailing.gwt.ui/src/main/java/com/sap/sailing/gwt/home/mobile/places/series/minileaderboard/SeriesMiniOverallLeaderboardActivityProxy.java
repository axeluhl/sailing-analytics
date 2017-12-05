package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ProvidesNavigationPath;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SeriesMiniOverallLeaderboardActivityProxy extends AbstractActivityProxy implements ProvidesNavigationPath {

    private final MobileApplicationClientFactory clientFactory;
    private final SeriesMiniOverallLeaderboardPlace currentPlace;
    private NavigationPathDisplay navigationPathDisplay;

    public SeriesMiniOverallLeaderboardActivityProxy(SeriesMiniOverallLeaderboardPlace place, MobileApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
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
                withFlagImageResolver(flagImageResolver -> new SeriesMiniOverallLeaderboardActivity(currentPlace, navigationPathDisplay, clientFactory, flagImageResolver));
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
