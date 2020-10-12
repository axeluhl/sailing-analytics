package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class AbstractAdvancedPlace extends AbstractAdminConsolePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.ADVANCED;
    }

}
