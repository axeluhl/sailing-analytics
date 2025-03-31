package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import java.util.function.Consumer;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;

public class IdleSixtyInchLeaderboardPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<IdleSixtyInchLeaderboardPlace> {
        @Override
        public String getToken(IdleSixtyInchLeaderboardPlace place) {
            return "";
        }

        @Override
        public IdleSixtyInchLeaderboardPlace getPlace(String token) {
            return new IdleSixtyInchLeaderboardPlace();
        }
    }

    private MultiRaceLeaderboardPanel leaderboardPanel;
    private CompetitorSelectionProvider competitorSelectionProvider;
    private Consumer<Integer> durationConsumer;

    public IdleSixtyInchLeaderboardPlace() {
    }

    public IdleSixtyInchLeaderboardPlace(MultiRaceLeaderboardPanel leaderboardPanel,
            CompetitorSelectionProvider competitorSelectionProvider, Consumer<Integer> durationConsumer) {
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
