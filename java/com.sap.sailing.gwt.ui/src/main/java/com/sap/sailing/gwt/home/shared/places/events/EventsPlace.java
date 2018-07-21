package com.sap.sailing.gwt.home.shared.places.events;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventsPlace extends Place implements HasLocationTitle, HasMobileVersion {
    public String getTitle() {
        return StringMessages.INSTANCE.sapSailing() + " - " + StringMessages.INSTANCE.events();
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
        return StringMessages.INSTANCE.events();
    }

}
