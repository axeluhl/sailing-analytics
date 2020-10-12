package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class IgtimiAccountsPlace extends AbstractConnectorsPlace {
    
    public IgtimiAccountsPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<IgtimiAccountsPlace> {
        @Override
        public String getToken(final IgtimiAccountsPlace place) {
            return "";
        }

        @Override
        public IgtimiAccountsPlace getPlace(final String token) {
            return new IgtimiAccountsPlace();
        }
    }
    
}
