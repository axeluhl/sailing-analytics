package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide6Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide6Place> {
        @Override
        public String getToken(Slide6Place place) {
            return "";
        }

        @Override
        public Slide6Place getPlace(String token) {
            return new Slide6Place();
        }
    }
}
