package com.sap.sailing.gwt.ui.adminconsole.places.regattas;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;

public class RegattasPlace extends AbstractAdminConsolePlace {
    
    public RegattasPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<RegattasPlace> {
        @Override
        public String getToken(final RegattasPlace place) {
            return "";
        }

        @Override
        public RegattasPlace getPlace(final String token) {
            return new RegattasPlace();
        }
    }

    @Override
    public String getVerticalTabName() {
        return "";
    }
    
}
