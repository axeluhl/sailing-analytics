package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide9;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide9Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide9Place> {
        @Override
        public String getToken(Slide9Place place) {
            return "";
        }

        @Override
        public Slide9Place getPlace(String token) {
            return new Slide9Place();
        }
    }
}
