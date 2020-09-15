package com.sap.sailing.gwt.ui.adminconsole.places;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class AdminConsolePlace extends Place {

    private String token;
    
    public AdminConsolePlace() {
        this.token = "testToken";
    }
    
    public String getToken() {
        return token;
    }
    
    public static class Tokenizer implements PlaceTokenizer<AdminConsolePlace> {
        @Override
        public String getToken(AdminConsolePlace place) {
            return place.getToken();
        }

        @Override
        public AdminConsolePlace getPlace(String token) {
            return new AdminConsolePlace();
        }
    }
    
}
