package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class ExpeditionDeviceConfigurationsPlace extends AbstractConnectorsPlace {
    
    public ExpeditionDeviceConfigurationsPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<ExpeditionDeviceConfigurationsPlace> {
        @Override
        public String getToken(final ExpeditionDeviceConfigurationsPlace place) {
            return "";
        }

        @Override
        public ExpeditionDeviceConfigurationsPlace getPlace(final String token) {
            return new ExpeditionDeviceConfigurationsPlace();
        }
    }
    
}
