package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide5;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide5Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide5Place> {
        @Override
        public String getToken(Slide5Place place) {
            return "";
        }

        @Override
        public Slide5Place getPlace(String token) {
            return new Slide5Place();
        }
    }
}
