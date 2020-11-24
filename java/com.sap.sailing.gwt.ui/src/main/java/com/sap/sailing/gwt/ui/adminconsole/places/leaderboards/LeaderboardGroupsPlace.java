package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.function.Function;

public class LeaderboardGroupsPlace extends AbstractLeaderboardsPlace {
    public LeaderboardGroupsPlace(String token) {
        super(token);
    }

    public static class Tokenizer extends TablePlaceTokenizer<LeaderboardGroupsPlace> {      
        @Override
        protected Function<String, LeaderboardGroupsPlace> getPlaceFactory() {
            return LeaderboardGroupsPlace::new;
        }
    }
}
