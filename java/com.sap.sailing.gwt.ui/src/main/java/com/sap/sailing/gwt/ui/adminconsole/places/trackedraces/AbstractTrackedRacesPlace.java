package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class AbstractTrackedRacesPlace extends AbstractAdminConsolePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.RACES;
    }

}
