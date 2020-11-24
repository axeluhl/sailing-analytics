package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public abstract class AbstractAdvancedPlace extends AbstractAdminConsolePlace {
    public AbstractAdvancedPlace(String token) {
        super(token);
    }

    // TODO bug5288: this method should not have static knowledge about which place is where; redundant with AdminConsoleViewImpl
    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.ADVANCED;
    }
}
