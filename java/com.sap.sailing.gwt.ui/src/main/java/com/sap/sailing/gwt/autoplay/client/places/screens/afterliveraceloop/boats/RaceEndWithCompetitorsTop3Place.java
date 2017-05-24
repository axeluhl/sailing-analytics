package com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats;

import com.google.gwt.place.shared.PlaceTokenizer;

public class RaceEndWithCompetitorsTop3Place extends AbstractRaceEndWithImagesTop3Place {
    public static class Tokenizer implements PlaceTokenizer<RaceEndWithCompetitorsTop3Place> {
        @Override
        public String getToken(RaceEndWithCompetitorsTop3Place place) {
            return "";
        }

        @Override
        public RaceEndWithCompetitorsTop3Place getPlace(String token) {
            return new RaceEndWithCompetitorsTop3Place();
        }
    }
}
