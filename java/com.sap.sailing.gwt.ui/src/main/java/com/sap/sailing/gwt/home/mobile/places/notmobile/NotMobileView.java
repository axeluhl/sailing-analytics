package com.sap.sailing.gwt.home.mobile.places.notmobile;

import com.google.gwt.user.client.ui.Widget;

public interface NotMobileView {
    
    public interface Presenter {
        void goBack();
    }

    Widget asWidget();
    void setGotoDesktopUrl(String string);
}

