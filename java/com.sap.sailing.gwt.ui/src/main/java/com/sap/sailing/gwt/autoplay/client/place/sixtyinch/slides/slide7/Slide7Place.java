package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Slide7Place extends Place {
    public static class Tokenizer implements PlaceTokenizer<Slide7Place> {
        @Override
        public String getToken(Slide7Place place) {
            return "";
        }

        @Override
        public Slide7Place getPlace(String token) {
            return new Slide7Place();
        }
    }
}
