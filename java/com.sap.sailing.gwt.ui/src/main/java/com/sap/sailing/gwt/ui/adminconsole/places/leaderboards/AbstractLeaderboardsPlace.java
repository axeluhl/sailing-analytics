package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public abstract class AbstractLeaderboardsPlace extends AbstractFilterablePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.LEADERBOARDS;
    }

}
