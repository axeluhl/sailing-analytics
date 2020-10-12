package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class RolesPlace extends AbstractAdvancedPlace {
    
    public RolesPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<RolesPlace> {
        @Override
        public String getToken(final RolesPlace place) {
            return "";
        }

        @Override
        public RolesPlace getPlace(final String token) {
            return new RolesPlace();
        }
    }
    
}
