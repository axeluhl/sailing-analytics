package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspective;

public class LeaderboardPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<LeaderboardPlace> {
        @Override
        public String getToken(LeaderboardPlace place) {
            return "";
        }

        @Override
        public LeaderboardPlace getPlace(String token) {
            return new LeaderboardPlace();
        }
    }

    private LeaderboardWithHeaderPerspective leaderboardPerspective;

    public LeaderboardWithHeaderPerspective getLeaderboardPerspective() {
        return leaderboardPerspective;
    }

    public void setLeaderboardPerspective(LeaderboardWithHeaderPerspective leaderboardPerspective) {
        this.leaderboardPerspective = leaderboardPerspective;
    }

}
