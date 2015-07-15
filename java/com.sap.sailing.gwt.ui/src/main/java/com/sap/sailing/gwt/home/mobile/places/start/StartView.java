package com.sap.sailing.gwt.home.mobile.places.start;

import com.google.gwt.user.client.ui.Widget;

public interface StartView {

    Widget asWidget();
    
    public interface Presenter {
        void gotoEvents();
    }
}

