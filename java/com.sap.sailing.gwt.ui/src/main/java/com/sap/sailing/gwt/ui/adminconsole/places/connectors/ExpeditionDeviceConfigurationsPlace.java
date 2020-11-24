package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class ExpeditionDeviceConfigurationsPlace extends AbstractFilterablePlace {
    public ExpeditionDeviceConfigurationsPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.CONNECTORS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<ExpeditionDeviceConfigurationsPlace> {      

        @Override
        protected Function<String, ExpeditionDeviceConfigurationsPlace> getPlaceFactory() {
            return ExpeditionDeviceConfigurationsPlace::new;
        }
    }
}
