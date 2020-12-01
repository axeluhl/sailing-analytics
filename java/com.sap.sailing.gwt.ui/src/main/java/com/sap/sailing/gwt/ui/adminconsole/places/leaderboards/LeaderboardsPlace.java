package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;

public class LeaderboardsPlace extends AbstractFilterablePlace {
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
