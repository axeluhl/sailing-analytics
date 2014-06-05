package com.sap.sailing.gwt.home.client.place.contact;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class ContactPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<ContactPlace> {
        @Override
        public String getToken(ContactPlace place) {
            return null;
        }

        @Override
        public ContactPlace getPlace(String token) {
            return new ContactPlace();
        }
    }
}
