package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ProvidesNavigationPath;
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
                super.onSuccess(new SeriesMiniOverallLeaderboardActivity(currentPlace, navigationPathDisplay, clientFactory));
            }
        });
    }
}
