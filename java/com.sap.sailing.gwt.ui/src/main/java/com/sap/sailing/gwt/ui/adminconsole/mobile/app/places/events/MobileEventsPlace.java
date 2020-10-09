package com.sap.sailing.gwt.ui.adminconsole.mobile.app.places.events;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.adminconsole.shared.app.HasMobileVersion;

public class MobileEventsPlace extends Place implements HasMobileVersion {

    public MobileEventsPlace() {
    }
    
    public static class Tokenizer implements PlaceTokenizer<MobileEventsPlace> {
        @Override
        public String getToken(final MobileEventsPlace place) {
            return "MobileEvents";
        }

        @Override
        public MobileEventsPlace getPlace(final String token) {
            return new MobileEventsPlace();
        }
    }
    
}
