package com.sap.sailing.gwt.autoplay.client.places.config;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class ConfigPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<ConfigPlace> {
        @Override
        public String getToken(ConfigPlace place) {
            return null;
        }

        @Override
        public ConfigPlace getPlace(String token) {
            return new ConfigPlace();
        }
    }
}
