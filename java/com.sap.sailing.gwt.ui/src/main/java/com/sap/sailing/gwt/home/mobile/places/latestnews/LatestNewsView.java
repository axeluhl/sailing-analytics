package com.sap.sailing.gwt.home.mobile.places.latestnews;

import com.google.gwt.user.client.ui.Widget;

public interface LatestNewsView {

    Widget asWidget();
    
    public interface Presenter extends NewsItemLinkProvider {
        void gotoEvents();
    }
}

