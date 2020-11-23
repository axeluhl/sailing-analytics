package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.function.Supplier;

public class LeaderboardGroupsPlace extends AbstractLeaderboardsPlace {

    public static class Tokenizer extends TablePlaceTokenizer<LeaderboardGroupsPlace> {      

        @Override
        protected Supplier<LeaderboardGroupsPlace> getPlaceFactory() {
            return LeaderboardGroupsPlace::new;
        }
    }
}
