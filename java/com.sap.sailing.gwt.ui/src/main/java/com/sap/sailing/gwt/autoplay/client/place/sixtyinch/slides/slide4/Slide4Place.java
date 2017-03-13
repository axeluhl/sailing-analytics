package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide4Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide4Place> {
        @Override
        public String getToken(Slide4Place place) {
            return "";
        }

        @Override
        public Slide4Place getPlace(String token) {
            return new Slide4Place();
        }
    }
}
