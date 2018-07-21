package com.sap.sailing.gwt.home.client.place.event.legacy;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventPlace extends AbstractBasePlace {
    private final String eventUuidAsString;
    private final String leaderboardIdAsNameString;
    private final EventNavigationTabs navigationTab;
    
    private final static String PARAM_EVENTID = "eventId"; 
    private final static String PARAM_LEADEROARD_NAME = "leaderboardName"; 
    private final static String PARAM_NAVIGATION_TAB = "navigationTab"; 

    public enum EventNavigationTabs { Overview, Regattas, Regatta, Media, Schedule };
    
    public EventPlace(String url) {
        super(url);
        eventUuidAsString = getParameter(PARAM_EVENTID);
        leaderboardIdAsNameString = getParameter(PARAM_LEADEROARD_NAME);
        String paramNavTab = getParameter(PARAM_NAVIGATION_TAB);
        if(paramNavTab != null) {
            navigationTab = EventNavigationTabs.valueOf(paramNavTab);
        } else {
            navigationTab = EventNavigationTabs.Regattas;
        }
    }

    public EventPlace(String eventUuidAsString, EventNavigationTabs navigationTab, String leaderboardIdAsNameString) {
        super(PARAM_EVENTID, eventUuidAsString, PARAM_NAVIGATION_TAB, navigationTab.name(), PARAM_LEADEROARD_NAME, leaderboardIdAsNameString);
        this.eventUuidAsString = eventUuidAsString;
        this.navigationTab = navigationTab;
        this.leaderboardIdAsNameString = leaderboardIdAsNameString;
    }

    public String getTitle(String eventName) {
        return StringMessages.INSTANCE.sapSailing() + " - " + eventName;
    }
    
    public String getEventUuidAsString() {
        return eventUuidAsString;
    }

    public String getLeaderboardIdAsNameString() {
        return leaderboardIdAsNameString;
    }

    public EventNavigationTabs getNavigationTab() {
        return navigationTab;
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
