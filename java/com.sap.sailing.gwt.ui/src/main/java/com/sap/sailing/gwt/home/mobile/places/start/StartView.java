package com.sap.sailing.gwt.home.mobile.places.start;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;

public interface StartView {

    Widget asWidget();
    
    public interface Presenter {
        MobilePlacesNavigator getNavigator();
    }
}

