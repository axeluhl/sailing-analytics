package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.Map;
import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class LeaderboardGroupsPlace extends AbstractFilterablePlace {
    public LeaderboardGroupsPlace(String token) {
        super(token);
    }

    public LeaderboardGroupsPlace(Map<String, String> paramKeysAndValues) {
        super(paramKeysAndValues);
    }
    
    // TODO bug5288 this method should not have static information that is redundant with AdminConsoleViewImpl and how it composes the panels in tabs
    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.LEADERBOARDS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<LeaderboardGroupsPlace> {      
        @Override
        protected Function<String, LeaderboardGroupsPlace> getPlaceFactory() {
            return LeaderboardGroupsPlace::new;
        }
    }
}
