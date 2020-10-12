package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class UserManagementPlace extends AbstractAdvancedPlace {
    
    public UserManagementPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<UserManagementPlace> {
        @Override
        public String getToken(final UserManagementPlace place) {
            return "";
        }

        @Override
        public UserManagementPlace getPlace(final String token) {
            return new UserManagementPlace();
        }
    }
    
}
