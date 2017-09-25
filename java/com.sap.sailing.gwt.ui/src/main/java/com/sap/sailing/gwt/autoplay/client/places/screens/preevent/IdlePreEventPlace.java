package com.sap.sailing.gwt.autoplay.client.places.screens.preevent;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class IdlePreEventPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<IdlePreEventPlace> {
        @Override
        public String getToken(IdlePreEventPlace place) {
            return "";
        }

        @Override
        public IdlePreEventPlace getPlace(String token) {
            return new IdlePreEventPlace();
        }
    }
}
