package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide0Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide0Place> {
        @Override
        public String getToken(Slide0Place place) {
            return "";
        }

        @Override
        public Slide0Place getPlace(String token) {
            return new Slide0Place();
        }
    }
}
