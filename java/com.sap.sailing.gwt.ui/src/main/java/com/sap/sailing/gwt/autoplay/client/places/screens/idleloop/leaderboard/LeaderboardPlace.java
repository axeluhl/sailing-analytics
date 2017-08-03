package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.MultiRaceLeaderboardWithZoomingPerspective;

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

    private MultiRaceLeaderboardWithZoomingPerspective leaderboardPerspective;

    public LeaderboardPlace(MultiRaceLeaderboardWithZoomingPerspective leaderboardPerspective) {
        this.leaderboardPerspective = leaderboardPerspective;
    }

    private LeaderboardPlace() {
    }

    public MultiRaceLeaderboardWithZoomingPerspective getLeaderboardPerspective() {
        return leaderboardPerspective;
    }


}
