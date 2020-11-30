package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class LeaderboardsPlace extends AbstractFilterablePlace {
    public LeaderboardsPlace(String token) {
        super(token);
    }

    // TODO bug5288 this method should not have static information that is redundant with AdminConsoleViewImpl and how it composes the panels in tabs
    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.LEADERBOARDS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<LeaderboardsPlace> {      
        @Override
        protected Function<String, LeaderboardsPlace> getPlaceFactory() {
            return LeaderboardsPlace::new;
        }
    }    
}
