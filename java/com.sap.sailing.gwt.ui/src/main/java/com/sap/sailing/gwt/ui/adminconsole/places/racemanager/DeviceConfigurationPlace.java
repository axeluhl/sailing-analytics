package com.sap.sailing.gwt.ui.adminconsole.places.racemanager;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;

public class DeviceConfigurationPlace extends AbstractAdminConsolePlace {
    
    public DeviceConfigurationPlace(String token) {
        super(token);
    }

    public static class Tokenizer implements PlaceTokenizer<DeviceConfigurationPlace> {
        @Override
        public String getToken(final DeviceConfigurationPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public DeviceConfigurationPlace getPlace(final String token) {
            return new DeviceConfigurationPlace(token);
        }
    }
}
