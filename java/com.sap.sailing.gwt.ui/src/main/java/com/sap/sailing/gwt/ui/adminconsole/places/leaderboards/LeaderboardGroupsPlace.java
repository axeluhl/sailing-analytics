package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.function.Supplier;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class LeaderboardGroupsPlace extends AbstractFilterablePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.LEADERBOARDS;
    }
    
    public LeaderboardGroupsPlace() {
    }

    public static class Tokenizer extends TablePlaceTokenizer<LeaderboardGroupsPlace> {      

        @Override
        protected Supplier<LeaderboardGroupsPlace> getPlaceFactory() {
            return LeaderboardGroupsPlace::new;
        }
    }
}
