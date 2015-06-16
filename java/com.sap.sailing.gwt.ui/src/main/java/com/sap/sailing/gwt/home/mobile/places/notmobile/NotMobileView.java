package com.sap.sailing.gwt.home.mobile.places.notmobile;

import com.google.gwt.user.client.ui.Widget;

public interface NotMobileView {

    Widget asWidget();
    
    public interface Presenter {
        void gotoEvents();

        void gotoDesktopVersion();

        void goBack();
    }
}

