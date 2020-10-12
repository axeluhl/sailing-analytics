package com.sap.sailing.gwt.ui.adminconsole.places;

import com.sap.sse.gwt.adminconsole.AdminConsolePlace;

public abstract class AbstractAdminConsolePlace extends AdminConsolePlace {
    
    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass();
    }
    
    public boolean isSameMenuItem(String menuItemName) {
        return getVerticalTabName().equals(menuItemName);
    }
}
