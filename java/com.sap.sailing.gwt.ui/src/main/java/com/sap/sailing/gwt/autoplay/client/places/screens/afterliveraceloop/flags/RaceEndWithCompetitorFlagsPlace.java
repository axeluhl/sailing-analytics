package com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.flags;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.AbstractRaceEndWithImagesTop3Place;

public class RaceEndWithCompetitorFlagsPlace extends AbstractRaceEndWithImagesTop3Place {
    public static class Tokenizer implements PlaceTokenizer<RaceEndWithCompetitorFlagsPlace> {
        @Override
        public String getToken(RaceEndWithCompetitorFlagsPlace place) {
            return "";
        }

        @Override
        public RaceEndWithCompetitorFlagsPlace getPlace(String token) {
            return new RaceEndWithCompetitorFlagsPlace();
        }
    }

}
