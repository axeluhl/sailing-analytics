package com.sap.sailing.gwt.home.client.place.event;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.AbstractBasePlace;

public class EventPlace extends AbstractBasePlace {
    private final String eventUuidAsString;
    private final String leaderboardIdAsNameString;
    
    public EventPlace(String url) {
        super(url);
        eventUuidAsString = getParameter("eventId");
        leaderboardIdAsNameString = getParameter("leaderboardName");
    }

    public EventPlace(String eventUuidAsString, String leaderboardIdAsNameString) {
        super("eventId", eventUuidAsString, "leaderboardName", leaderboardIdAsNameString);
        this.eventUuidAsString = eventUuidAsString;
        this.leaderboardIdAsNameString = leaderboardIdAsNameString;
    }

    public String getEventUuidAsString() {
        return eventUuidAsString;
    }

    public String getLeaderboardIdAsNameString() {
        return leaderboardIdAsNameString;
    }

    public static class Tokenizer implements PlaceTokenizer<EventPlace> {
        @Override
        public String getToken(EventPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public EventPlace getPlace(String url) {
            return new EventPlace(url);
        }
    }
}
