package com.sap.sailing.gwt.home.desktop.places.searchresult;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SearchResultActivityProxy extends AbstractActivityProxy {

    private final SearchResultClientFactory clientFactory;
    private final SearchResultPlace place;

    public SearchResultActivityProxy(SearchResultPlace place, SearchResultClientFactory clientFactory) {
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
