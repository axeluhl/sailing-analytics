package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide3;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide3Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide3Place> {
        @Override
        public String getToken(Slide3Place place) {
            return "";
        }

        @Override
        public Slide3Place getPlace(String token) {
            return new Slide3Place();
        }
    }
}
