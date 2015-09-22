package com.sap.sailing.gwt.home.desktop.places.events;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;

public class EventsPlace extends Place implements HasLocationTitle, HasMobileVersion {
    public String getTitle() {
        return TextMessages.INSTANCE.sapSailing() + " - " + TextMessages.INSTANCE.events();
    }
    
    public static class Tokenizer implements PlaceTokenizer<EventsPlace> {
        @Override
        public String getToken(EventsPlace place) {
            return "";
        }

        @Override
        public EventsPlace getPlace(String token) {
            return new EventsPlace();
        }
    }

    @Override
    public String getLocationTitle() {
        return TextMessages.INSTANCE.events();
    }

}
