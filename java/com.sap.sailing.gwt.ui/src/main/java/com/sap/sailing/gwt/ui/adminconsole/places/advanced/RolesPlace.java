package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class RolesPlace extends AbstractFilterablePlace {
    public RolesPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.ADVANCED;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<RolesPlace> {      
        @Override
        protected Function<String, RolesPlace> getPlaceFactory() {
            return RolesPlace::new;
        }
    }
}
