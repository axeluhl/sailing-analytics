package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class RemoteServerInstancesPlace extends AbstractAdvancedPlace {
    
    public RemoteServerInstancesPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<RemoteServerInstancesPlace> {
        @Override
        public String getToken(final RemoteServerInstancesPlace place) {
            return "";
        }

        @Override
        public RemoteServerInstancesPlace getPlace(final String token) {
            return new RemoteServerInstancesPlace();
        }
    }
    
}
