package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;

public class LeaderboardPlace extends AbstractBasePlace {
    private final String eventUuidAsString;
    private final String leaderboardIdAsNameString;
    private final Boolean showRaceDetails;
    private final Boolean showSettings;
    
    private final static String PARAM_EVENTID = "eventId";
    private final static String PARAM_LEADERBOARD_NAME = "leaderboardName";
    private final static String PARAM_SHOW_RACE_DETAILS = "showRaceDetails";
    private final static String PARAM_SHOW_SETTINGS = "showSettings";
    
    public LeaderboardPlace(String url) {
        super(url);
        eventUuidAsString = getParameter(PARAM_EVENTID);
        leaderboardIdAsNameString = getParameter(PARAM_LEADERBOARD_NAME);
        showRaceDetails = Boolean.valueOf(getParameter(PARAM_SHOW_RACE_DETAILS));
        showSettings = Boolean.valueOf(getParameter(PARAM_SHOW_SETTINGS));
    }

    public LeaderboardPlace(String eventUuidAsString, String leaderboardIdAsNameString, Boolean showRaceDetails, Boolean showSettings) {
        super(PARAM_EVENTID, eventUuidAsString, PARAM_LEADERBOARD_NAME, leaderboardIdAsNameString, 
                PARAM_SHOW_RACE_DETAILS, String.valueOf(showRaceDetails), PARAM_SHOW_SETTINGS,  String.valueOf(showSettings));
        this.eventUuidAsString = eventUuidAsString;
        this.leaderboardIdAsNameString = leaderboardIdAsNameString;
        this.showRaceDetails = showRaceDetails;
        this.showSettings = showSettings;
    }

    public String getTitle(String eventName, String leaderboardName) {
        return TextMessages.INSTANCE.sapSailing() + " - " + TextMessages.INSTANCE.leaderboard() + ": " + leaderboardName;
    }
    
    public String getEventUuidAsString() {
        return eventUuidAsString;
    }

    public String getLeaderboardIdAsNameString() {
        return leaderboardIdAsNameString;
    }

    public Boolean getShowRaceDetails() {
        return showRaceDetails;
    }

    public Boolean getShowSettings() {
        return showSettings;
    }

    public static class Tokenizer implements PlaceTokenizer<LeaderboardPlace> {
        @Override
        public String getToken(LeaderboardPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public LeaderboardPlace getPlace(String url) {
            return new LeaderboardPlace(url);
        }
    }
}
