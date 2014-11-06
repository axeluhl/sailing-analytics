package com.sap.sailing.gwt.home.client.place.solutions.analytics;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class SailingAnalyticsPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<SailingAnalyticsPlace> {
        @Override
        public String getToken(SailingAnalyticsPlace place) {
            return null;
        }

        @Override
        public SailingAnalyticsPlace getPlace(String token) {
            return new SailingAnalyticsPlace();
        }
    }
}
