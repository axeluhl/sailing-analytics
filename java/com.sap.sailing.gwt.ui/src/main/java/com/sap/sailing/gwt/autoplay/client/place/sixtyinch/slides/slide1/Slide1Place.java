package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide1Place extends Place {

    public static class Tokenizer implements PlaceTokenizer<Slide1Place> {
        @Override
        public String getToken(Slide1Place place) {
            return "";
        }

        @Override
        public Slide1Place getPlace(String token) {
            return new Slide1Place();
        }
    }
}
