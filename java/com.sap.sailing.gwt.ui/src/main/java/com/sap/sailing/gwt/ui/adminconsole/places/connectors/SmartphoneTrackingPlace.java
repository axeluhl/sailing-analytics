package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class SmartphoneTrackingPlace extends AbstractFilterablePlace {
    public SmartphoneTrackingPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.CONNECTORS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<SmartphoneTrackingPlace> {      
        @Override
        protected Function<String, SmartphoneTrackingPlace> getPlaceFactory() {
            return SmartphoneTrackingPlace::new;
        }
    }
}
