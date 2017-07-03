package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;

public class IdleOverallLeaderBoardPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<IdleOverallLeaderBoardPlace> {
        @Override
        public String getToken(IdleOverallLeaderBoardPlace place) {
            return "";
        }

        @Override
        public IdleOverallLeaderBoardPlace getPlace(String token) {
            return new IdleOverallLeaderBoardPlace();
        }
    }


    private LeaderboardPanel leaderboardPanel;


    public IdleOverallLeaderBoardPlace() {
    }


    public IdleOverallLeaderBoardPlace(LeaderboardPanel leaderboardPanel) {
        this.leaderboardPanel = leaderboardPanel;
    }
    
    public LeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }
}
