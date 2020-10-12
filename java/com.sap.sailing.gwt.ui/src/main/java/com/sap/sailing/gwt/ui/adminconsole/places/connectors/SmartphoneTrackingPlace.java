package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class SmartphoneTrackingPlace extends AbstractConnectorsPlace {
    
    public SmartphoneTrackingPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<SmartphoneTrackingPlace> {
        @Override
        public String getToken(final SmartphoneTrackingPlace place) {
            return "";
        }

        @Override
        public SmartphoneTrackingPlace getPlace(final String token) {
            return new SmartphoneTrackingPlace();
        }
    }
    
}
