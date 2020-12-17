package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;

public class LandscapeManagementPlace extends AbstractFilterablePlace {
    public LandscapeManagementPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<LandscapeManagementPlace> {      
        @Override
        protected Function<String, LandscapeManagementPlace> getPlaceFactory() {
            return LandscapeManagementPlace::new;
        }
    }
}
