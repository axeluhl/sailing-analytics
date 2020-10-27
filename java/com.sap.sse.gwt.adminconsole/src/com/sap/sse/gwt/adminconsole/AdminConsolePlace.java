package com.sap.sse.gwt.adminconsole;

import com.google.gwt.place.shared.Place;

public abstract class AdminConsolePlace extends Place {

    public abstract String getVerticalTabName();
    
    public boolean isSamePlace(Object obj) {
        return obj != null && obj.getClass() == this.getClass();
    }

}