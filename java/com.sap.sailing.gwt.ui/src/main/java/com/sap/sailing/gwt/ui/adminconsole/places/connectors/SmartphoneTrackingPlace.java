package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class SmartphoneTrackingPlace extends AbstractFilterablePlace {
    public SmartphoneTrackingPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<SmartphoneTrackingPlace> {      
        @Override
        protected Function<String, SmartphoneTrackingPlace> getPlaceFactory() {
            return SmartphoneTrackingPlace::new;
        }
    }
}
