package com.sap.sailing.gwt.home.mobile.places.series;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ProvidesNavigationPath;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SeriesActivityProxy extends AbstractActivityProxy implements ProvidesNavigationPath {

    private final MobileApplicationClientFactory clientFactory;
    private final AbstractSeriesPlace currentPlace;
    private NavigationPathDisplay navigationPathDisplay;

    public SeriesActivityProxy(AbstractSeriesPlace place, MobileApplicationClientFactory clientFactory) {
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
                super.onSuccess(new SeriesActivity(currentPlace, navigationPathDisplay, clientFactory));
            }
        });
    }
}
