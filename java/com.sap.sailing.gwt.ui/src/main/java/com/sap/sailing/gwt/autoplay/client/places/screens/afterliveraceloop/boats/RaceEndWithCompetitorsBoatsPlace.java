package com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats;

import com.google.gwt.place.shared.PlaceTokenizer;

public class RaceEndWithCompetitorsBoatsPlace extends AbstractRaceEndWithImagesTop3Place {
    public static class Tokenizer implements PlaceTokenizer<RaceEndWithCompetitorsBoatsPlace> {
        @Override
        public String getToken(RaceEndWithCompetitorsBoatsPlace place) {
            return "";
        }

        @Override
        public RaceEndWithCompetitorsBoatsPlace getPlace(String token) {
            return new RaceEndWithCompetitorsBoatsPlace();
        }
    }
}
