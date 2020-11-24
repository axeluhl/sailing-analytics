package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import java.util.Map;
import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class CompetitorsPlace extends AbstractFilterablePlace {
    public CompetitorsPlace(Map<String, String> paramKeysAndValues) {
        super(paramKeysAndValues);
    }
    
    protected CompetitorsPlace(String token) {
        super(token);
    }
    
    // TODO bug5288 redundant with how AdminConsoleViewImpl assembles panels in tabs
    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.RACES;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<CompetitorsPlace> {      
        @Override
        protected Function<String, CompetitorsPlace> getPlaceFactory() {
            return CompetitorsPlace::new;
        }
    }
}
