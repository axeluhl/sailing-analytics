package com.sap.sailing.gwt.home.shared.places.start;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class StartPlace extends Place implements HasLocationTitle, HasMobileVersion {
    public String getTitle() {
        return StringMessages.INSTANCE.sapSailing();
    }
    
    public static class Tokenizer implements PlaceTokenizer<StartPlace> {
        @Override
        public String getToken(StartPlace place) {
            return null;
        }

        @Override
        public StartPlace getPlace(String token) {
            return new StartPlace();
        }
    }

    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.headerLogo();
    }
}
