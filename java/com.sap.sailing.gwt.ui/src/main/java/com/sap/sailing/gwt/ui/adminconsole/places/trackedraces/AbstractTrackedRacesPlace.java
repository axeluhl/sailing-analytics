package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public abstract class AbstractTrackedRacesPlace extends AbstractAdminConsolePlace {
    public AbstractTrackedRacesPlace(String placeParamsFromUrlFragment) {
        super(placeParamsFromUrlFragment);
    }

    // TODO bug5288 redundant with how AdminConsoleViewImpl assembles panels in tabs
    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.RACES;
    }
}
