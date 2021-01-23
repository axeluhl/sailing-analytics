package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.Map;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;

public abstract class AbstractLeaderboardsPlace extends AbstractFilterablePlace {
    public AbstractLeaderboardsPlace(String token) {
        super(token);
    }

    public AbstractLeaderboardsPlace(Map<String, String> paramKeysAndValues) {
        super(paramKeysAndValues);
    }
}
