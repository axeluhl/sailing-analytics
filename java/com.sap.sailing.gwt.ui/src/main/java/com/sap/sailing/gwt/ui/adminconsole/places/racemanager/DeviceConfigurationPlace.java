package com.sap.sailing.gwt.ui.adminconsole.places.racemanager;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class DeviceConfigurationPlace extends AbstractAdminConsolePlace {
    
    public DeviceConfigurationPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<DeviceConfigurationPlace> {
        @Override
        public String getToken(final DeviceConfigurationPlace place) {
            return "";
        }

        @Override
        public DeviceConfigurationPlace getPlace(final String token) {
            return new DeviceConfigurationPlace();
        }
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.RACE_COMMITEE;
    }
    
}
