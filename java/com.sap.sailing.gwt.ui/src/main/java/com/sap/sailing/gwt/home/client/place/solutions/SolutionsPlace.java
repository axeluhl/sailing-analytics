package com.sap.sailing.gwt.home.client.place.solutions;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class SolutionsPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<SolutionsPlace> {
        @Override
        public String getToken(SolutionsPlace place) {
            return null;
        }

        @Override
        public SolutionsPlace getPlace(String token) {
            return new SolutionsPlace();
        }
    }
}
