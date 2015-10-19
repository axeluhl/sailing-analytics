package com.sap.sailing.gwt.home.mobile.places.searchresult;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.desktop.places.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SearchResultActivityProxy extends AbstractActivityProxy {

    private final MobileApplicationClientFactory clientFactory;
    private final SearchResultPlace place;

    public SearchResultActivityProxy(SearchResultPlace place, MobileApplicationClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new SearchResultActivity(place, clientFactory));
            }
        });
    }
}
