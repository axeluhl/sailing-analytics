package com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.raceboard;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;

public class LifeRaceWithRaceboardPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<LifeRaceWithRaceboardPlace> {
        @Override
        public String getToken(LifeRaceWithRaceboardPlace place) {
            return "";
        }

        @Override
        public LifeRaceWithRaceboardPlace getPlace(String token) {
            return new LifeRaceWithRaceboardPlace();
        }
    }

    private RaceBoardPanel raceBoardPanel;
    private Throwable error;

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
