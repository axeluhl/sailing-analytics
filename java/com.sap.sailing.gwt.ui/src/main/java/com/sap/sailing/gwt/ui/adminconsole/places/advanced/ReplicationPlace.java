package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class ReplicationPlace extends AbstractAdvancedPlace {
    
    public ReplicationPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<ReplicationPlace> {
        @Override
        public String getToken(final ReplicationPlace place) {
            return "";
        }

        @Override
        public ReplicationPlace getPlace(final String token) {
            return new ReplicationPlace();
        }
    }
    
}
