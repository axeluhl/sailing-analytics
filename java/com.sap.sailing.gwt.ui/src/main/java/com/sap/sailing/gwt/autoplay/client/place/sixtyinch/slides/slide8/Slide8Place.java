package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide8Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide8Place> {
        @Override
        public String getToken(Slide8Place place) {
            return "";
        }

        @Override
        public Slide8Place getPlace(String token) {
            return new Slide8Place();
        }
    }
}
