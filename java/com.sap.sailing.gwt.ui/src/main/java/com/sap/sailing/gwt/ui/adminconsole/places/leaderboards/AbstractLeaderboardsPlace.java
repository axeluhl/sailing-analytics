package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class AbstractLeaderboardsPlace extends AbstractAdminConsolePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.LEADERBOARDS;
    }

}
