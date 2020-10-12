package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import com.google.gwt.place.shared.PlaceTokenizer;

public class LeaderboardsPlace extends AbstractLeaderboardsPlace {
    
    public LeaderboardsPlace() {
    }
    
    public static class Tokenizer implements PlaceTokenizer<LeaderboardsPlace> {
        @Override
        public String getToken(final LeaderboardsPlace place) {
            return "";
        }

        @Override
        public LeaderboardsPlace getPlace(final String token) {
            return new LeaderboardsPlace();
        }
    }
    
}
