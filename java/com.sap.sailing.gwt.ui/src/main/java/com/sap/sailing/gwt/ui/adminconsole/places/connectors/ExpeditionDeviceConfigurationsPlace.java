package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class ExpeditionDeviceConfigurationsPlace extends AbstractFilterablePlace {
    public ExpeditionDeviceConfigurationsPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<ExpeditionDeviceConfigurationsPlace> {      

        @Override
        protected Function<String, ExpeditionDeviceConfigurationsPlace> getPlaceFactory() {
            return ExpeditionDeviceConfigurationsPlace::new;
        }
    }
}
