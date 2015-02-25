package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;

public class RegattaRacesPlace extends AbstractEventRegattaPlace {
    public RegattaRacesPlace(String id, String leaderboardName) {
        super(id, leaderboardName);
    }
    
    public RegattaRacesPlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer extends AbstractEventRegattaPlace.Tokenizer<RegattaRacesPlace> {
        @Override
        protected RegattaRacesPlace getRealPlace(String eventId, String leaderboardName) {
            return new RegattaRacesPlace(eventId, leaderboardName);
        }
    }
}
