package com.sap.sailing.gwt.ui.adminconsole.places.leaderboards;

import java.util.Map;
import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class LeaderboardGroupsPlace extends AbstractFilterablePlace {
    public LeaderboardGroupsPlace(String token) {
        super(token);
    }

    public LeaderboardGroupsPlace(Map<String, String> paramKeysAndValues) {
        super(paramKeysAndValues);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<LeaderboardGroupsPlace> {      
        @Override
        protected Function<String, LeaderboardGroupsPlace> getPlaceFactory() {
            return LeaderboardGroupsPlace::new;
        }
    }
}
