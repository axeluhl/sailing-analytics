package com.sap.sse.gwt.adminconsole;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;

public class DefaultPlacesAwareAdminConsolePanel<W extends Widget> implements PlaceAwareAdminConsolePanel {
   
    Place place;

    public Place getPlace() {
        return place;
    }

    private final W widget;
    
    public DefaultPlacesAwareAdminConsolePanel(W widget, Place place) {
        this.widget = widget;
        this.place = place;
    }

}
