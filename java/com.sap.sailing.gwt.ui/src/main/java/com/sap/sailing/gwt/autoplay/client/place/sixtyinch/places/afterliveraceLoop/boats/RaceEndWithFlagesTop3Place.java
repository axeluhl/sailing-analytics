package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.afterliveraceLoop.boats;

import com.google.gwt.place.shared.PlaceTokenizer;

public class RaceEndWithFlagesTop3Place extends AbstractRaceEndWithImagesTop3Place {
    public static class Tokenizer implements PlaceTokenizer<RaceEndWithFlagesTop3Place> {
        @Override
        public String getToken(RaceEndWithFlagesTop3Place place) {
            return "";
        }

        @Override
        public RaceEndWithFlagesTop3Place getPlace(String token) {
            return new RaceEndWithFlagesTop3Place();
        }
    }

}
