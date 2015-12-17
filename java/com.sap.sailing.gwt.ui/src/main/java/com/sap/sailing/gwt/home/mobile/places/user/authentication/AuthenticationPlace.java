package com.sap.sailing.gwt.home.mobile.places.user.authentication;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;

public class AuthenticationPlace extends Place implements HasMobileVersion {

    public static class Tokenizer implements PlaceTokenizer<AuthenticationPlace> {
        @Override
        public String getToken(AuthenticationPlace place) {
            return null;
        }

        @Override
        public AuthenticationPlace getPlace(String token) {
            return new AuthenticationPlace();
        }
    }
    
}
