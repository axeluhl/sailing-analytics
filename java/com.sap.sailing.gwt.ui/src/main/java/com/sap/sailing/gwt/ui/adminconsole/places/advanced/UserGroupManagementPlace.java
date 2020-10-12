package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class UserGroupManagementPlace extends AbstractAdvancedPlace {
    
    public UserGroupManagementPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<UserGroupManagementPlace> {
        @Override
        public String getToken(final UserGroupManagementPlace place) {
            return "";
        }

        @Override
        public UserGroupManagementPlace getPlace(final String token) {
            return new UserGroupManagementPlace();
        }
    }
    
}
