package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Supplier;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class ExpeditionDeviceConfigurationsPlace extends AbstractFilterablePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.CONNECTORS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<ExpeditionDeviceConfigurationsPlace> {      

        @Override
        protected Supplier<ExpeditionDeviceConfigurationsPlace> getPlaceFactory() {
            return ExpeditionDeviceConfigurationsPlace::new;
        }
    }
}
