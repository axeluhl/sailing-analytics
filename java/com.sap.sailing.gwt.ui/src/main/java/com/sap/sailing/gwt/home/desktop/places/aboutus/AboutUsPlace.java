package com.sap.sailing.gwt.home.desktop.places.aboutus;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class AboutUsPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<AboutUsPlace> {
        @Override
        public String getToken(AboutUsPlace place) {
            return null;
        }

        @Override
        public AboutUsPlace getPlace(String token) {
            return new AboutUsPlace();
        }
    }
}
