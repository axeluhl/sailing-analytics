package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import com.google.gwt.place.shared.PlaceTokenizer;

public class LeaderboardGroupsPlace extends AbstractLeaderboardsPlace {

    
    public LeaderboardGroupsPlace() {
    }

    public static class Tokenizer implements PlaceTokenizer<LeaderboardGroupsPlace> {
        @Override
        public String getToken(final LeaderboardGroupsPlace place) {
            return "";
        }

        @Override
        public LeaderboardGroupsPlace getPlace(final String token) {
            return new LeaderboardGroupsPlace();
        }
    }
    
}
