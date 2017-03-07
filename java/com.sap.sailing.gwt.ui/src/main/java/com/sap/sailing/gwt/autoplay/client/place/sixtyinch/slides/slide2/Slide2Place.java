package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide2Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide2Place> {
        @Override
        public String getToken(Slide2Place place) {
            return "";
        }

        @Override
        public Slide2Place getPlace(String token) {
            return new Slide2Place();
        }
    }
}
