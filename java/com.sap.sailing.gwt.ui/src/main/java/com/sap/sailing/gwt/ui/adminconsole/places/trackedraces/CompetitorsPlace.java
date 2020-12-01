package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import java.util.Map;
import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;

public class CompetitorsPlace extends AbstractFilterablePlace {
    public CompetitorsPlace(Map<String, String> paramKeysAndValues) {
        super(paramKeysAndValues);
    }
    
    protected CompetitorsPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<CompetitorsPlace> {      
        @Override
        protected Function<String, CompetitorsPlace> getPlaceFactory() {
            return CompetitorsPlace::new;
        }
    }
}
