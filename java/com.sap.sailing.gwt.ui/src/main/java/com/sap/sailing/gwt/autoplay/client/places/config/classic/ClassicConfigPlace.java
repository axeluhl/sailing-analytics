package com.sap.sailing.gwt.autoplay.client.places.config.classic;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class ClassicConfigPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<ClassicConfigPlace> {
        @Override
        public String getToken(ClassicConfigPlace place) {
            return null;
        }

        @Override
        public ClassicConfigPlace getPlace(String token) {
            return new ClassicConfigPlace();
        }
    }
}
