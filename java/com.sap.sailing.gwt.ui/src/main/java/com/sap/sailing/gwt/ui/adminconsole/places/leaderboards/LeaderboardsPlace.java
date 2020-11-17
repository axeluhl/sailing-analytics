package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.function.Supplier;

public class LeaderboardsPlace extends AbstractLeaderboardsPlace {
    
    public static class Tokenizer extends TablePlaceTokenizer<LeaderboardsPlace> {      

        @Override
        protected Supplier<LeaderboardsPlace> getPlaceFactory() {
            return LeaderboardsPlace::new;
        }
    }    
}
