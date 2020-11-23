package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import java.util.function.Supplier;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class CompetitorsPlace extends AbstractFilterablePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.RACES;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<CompetitorsPlace> {      

        @Override
        protected Supplier<CompetitorsPlace> getPlaceFactory() {
            return CompetitorsPlace::new;
        }
    }
}
