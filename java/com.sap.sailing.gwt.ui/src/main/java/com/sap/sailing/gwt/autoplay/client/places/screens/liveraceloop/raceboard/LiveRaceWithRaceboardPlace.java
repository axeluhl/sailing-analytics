package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;

public class LiveRaceWithRaceboardPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<LiveRaceWithRaceboardPlace> {
        @Override
        public String getToken(LiveRaceWithRaceboardPlace place) {
            return "";
        }

        @Override
        public LiveRaceWithRaceboardPlace getPlace(String token) {
            return new LiveRaceWithRaceboardPlace();
        }
    }

    private RaceBoardPanel raceBoardPanel;
    private Throwable error;

    public LiveRaceWithRaceboardPlace(RaceBoardPanel raceBoardPanel) {
        this.raceBoardPanel = raceBoardPanel;
    }

    private LiveRaceWithRaceboardPlace() {
    }

    public void setRaceMap(RaceBoardPanel result) {
        this.raceBoardPanel = result;
    }

    public RaceBoardPanel getRaceBoardPanel() {
        return raceBoardPanel;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable caught) {
        this.error = caught;
    }
}
