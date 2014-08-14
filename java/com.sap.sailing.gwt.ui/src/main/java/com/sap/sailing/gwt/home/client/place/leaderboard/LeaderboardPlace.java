package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.AbstractBasePlace;

public class LeaderboardPlace extends AbstractBasePlace {
    private final String eventUuidAsString;
    private final String leaderboardIdAsNameString;
    
    public LeaderboardPlace(String url) {
        super(url);
        eventUuidAsString = getParameter("eventId");
        leaderboardIdAsNameString = getParameter("leaderboardName");
    }

    public LeaderboardPlace(String eventUuidAsString, String leaderboardIdAsNameString) {
        super("eventId", eventUuidAsString, "leaderboardName", leaderboardIdAsNameString);
        this.eventUuidAsString = eventUuidAsString;
        this.leaderboardIdAsNameString = leaderboardIdAsNameString;
    }

    public String getEventUuidAsString() {
        return eventUuidAsString;
    }

    public String getLeaderboardIdAsNameString() {
        return leaderboardIdAsNameString;
    }

    public static class Tokenizer implements PlaceTokenizer<LeaderboardPlace> {
        @Override
        public String getToken(LeaderboardPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public LeaderboardPlace getPlace(String url) {
            return new LeaderboardPlace(url);
        }
    }
}
