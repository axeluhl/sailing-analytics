package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import java.util.function.Consumer;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;

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


    private MultiRaceLeaderboardPanel leaderboardPanel;
    private CompetitorSelectionProvider competitorSelectionProvider;
    private Consumer<Integer> durationConsumer;

    public IdleOverallLeaderBoardPlace() {
    }


    public IdleOverallLeaderBoardPlace(MultiRaceLeaderboardPanel leaderboardPanel, CompetitorSelectionProvider competitorSelectionProvider, Consumer<Integer> durationConsumer) {
        this.leaderboardPanel = leaderboardPanel;
        this.competitorSelectionProvider = competitorSelectionProvider;
        this.durationConsumer = durationConsumer;
    }
    
    public Consumer<Integer> getDurationConsumer() {
        return durationConsumer;
    }
    
    public CompetitorSelectionProvider getCompetitorSelectionProvider() {
        return competitorSelectionProvider;
    }
    
    public MultiRaceLeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }
}
