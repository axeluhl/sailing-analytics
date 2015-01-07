package com.sap.sailing.gwt.home.client.place.whatsnew;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class WhatsNewPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<WhatsNewPlace> {
        @Override
        public String getToken(WhatsNewPlace place) {
            return null;
        }

        @Override
        public WhatsNewPlace getPlace(String token) {
            return new WhatsNewPlace();
        }
    }
}
