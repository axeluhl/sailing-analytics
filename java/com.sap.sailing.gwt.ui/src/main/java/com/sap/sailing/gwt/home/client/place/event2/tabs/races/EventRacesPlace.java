package com.sap.sailing.gwt.home.client.place.event2.tabs.races;

import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventRacesPlace extends AbstractEventRegattaPlace {
    public EventRacesPlace(String id, String leaderboardName) {
        super(id, leaderboardName);
    }
    
    public EventRacesPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractEventRegattaPlace.Tokenizer<EventRacesPlace> {
        @Override
        protected EventRacesPlace getRealPlace(String eventId, String leaderboardName) {
            return new EventRacesPlace(eventId, leaderboardName);
        }
    }
}
