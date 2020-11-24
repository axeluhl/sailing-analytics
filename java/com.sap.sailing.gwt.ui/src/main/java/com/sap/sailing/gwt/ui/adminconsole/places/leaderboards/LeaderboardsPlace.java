package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.function.Function;

public class LeaderboardsPlace extends AbstractLeaderboardsPlace {
    public LeaderboardsPlace(String token) {
        super(token);
    }

    public static class Tokenizer extends TablePlaceTokenizer<LeaderboardsPlace> {      
        @Override
        protected Function<String, LeaderboardsPlace> getPlaceFactory() {
            return LeaderboardsPlace::new;
        }
    }    
}
