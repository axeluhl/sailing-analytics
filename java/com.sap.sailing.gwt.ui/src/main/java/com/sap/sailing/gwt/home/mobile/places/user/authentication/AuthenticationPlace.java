package com.sap.sailing.gwt.home.mobile.places.user.authentication;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sse.security.ui.authentication.AuthenticationPlaces;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class AuthenticationPlace extends Place implements HasMobileVersion, HasLocationTitle {

    private final AuthenticationPlaces requestedPlace;

    public AuthenticationPlace(AuthenticationPlaces requestedPlace) {
        this.requestedPlace = requestedPlace;
    }
    
    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.user();
    }
    
    public AuthenticationPlaces getRequestedPlace() {
        return requestedPlace;
    }

    @Prefix(PlaceTokenPrefixes.UserAuthentication)
    public static class Tokenizer implements PlaceTokenizer<AuthenticationPlace> {
        @Override
        public String getToken(AuthenticationPlace place) {
            return "";
        }

        @Override
        public AuthenticationPlace getPlace(String token) {
            return new AuthenticationPlace(AuthenticationPlaces.SIGN_IN);
        }
    }
    
}
