package com.sap.sailing.gwt.home.desktop.places.sponsoring;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class SponsoringPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<SponsoringPlace> {
        @Override
        public String getToken(SponsoringPlace place) {
            return null;
        }

        @Override
        public SponsoringPlace getPlace(String token) {
            return new SponsoringPlace();
        }
    }
}
