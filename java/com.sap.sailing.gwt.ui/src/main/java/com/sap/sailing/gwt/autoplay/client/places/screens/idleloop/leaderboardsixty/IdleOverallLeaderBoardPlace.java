package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;

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


    private SixtyInchLeaderBoard leaderboardPanel;


    public IdleOverallLeaderBoardPlace() {
    }


    public IdleOverallLeaderBoardPlace(SixtyInchLeaderBoard leaderboardPanel) {
        this.leaderboardPanel = leaderboardPanel;
    }
    
    public SixtyInchLeaderBoard getLeaderboardPanel() {
        return leaderboardPanel;
    }
}
