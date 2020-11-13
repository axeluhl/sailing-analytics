package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.function.Supplier;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class LeaderboardsPlace extends AbstractFilterablePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.LEADERBOARDS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<LeaderboardsPlace> {      

        @Override
        protected Supplier<LeaderboardsPlace> getPlaceFactory() {
            return LeaderboardsPlace::new;
        }
    }    
}
