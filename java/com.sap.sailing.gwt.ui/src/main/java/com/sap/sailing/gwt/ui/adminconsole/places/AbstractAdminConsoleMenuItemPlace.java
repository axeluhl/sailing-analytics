package com.sap.sailing.gwt.ui.adminconsole.places;

import com.google.gwt.place.shared.Place;

public class AbstractAdminConsoleMenuItemPlace extends Place {

    protected String tab;
    
    protected AbstractAdminConsoleMenuItemPlace(final String tab) {
        this.tab = tab;
    }
    
    public String getTab() {
        return tab;
    }
    
}
