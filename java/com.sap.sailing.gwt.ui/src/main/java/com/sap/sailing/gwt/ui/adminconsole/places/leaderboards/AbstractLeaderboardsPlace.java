package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public abstract class AbstractLeaderboardsPlace extends AbstractFilterablePlace {
    public AbstractLeaderboardsPlace(String token) {
        super(token);
    }

    // TODO bug5288 this method should not have static information that is redundant with AdminConsoleViewImpl and how it composes the panels in tabs
    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.LEADERBOARDS;
    }
}
