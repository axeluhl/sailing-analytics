package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;

public class EventsPlace extends Place {
    public String getTitle() {
        return TextMessages.INSTANCE.sapSailing() + " - " + TextMessages.INSTANCE.events();
    }
    
    public static class Tokenizer implements PlaceTokenizer<EventsPlace> {
        @Override
        public String getToken(EventsPlace place) {
            return null;
        }

        @Override
        public EventsPlace getPlace(String token) {
            return new EventsPlace();
        }
    }
}
