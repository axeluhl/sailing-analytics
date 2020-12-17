package com.sap.sailing.landscape.ui;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

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
