package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class SlideInitPlace extends Place {

    public static class Tokenizer implements PlaceTokenizer<SlideInitPlace> {
        @Override
        public String getToken(SlideInitPlace place) {
            return "";
        }

        @Override
        public SlideInitPlace getPlace(String token) {
            return new SlideInitPlace();
        }
    }
}
