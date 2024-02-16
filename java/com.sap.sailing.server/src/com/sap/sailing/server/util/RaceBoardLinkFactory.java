package com.sap.sailing.server.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;

public class RaceBoardLinkFactory {
    private final static URL defaultBaseURL;
    
    static {
        try {
            defaultBaseURL = new URL("https://www.sapsailing.com");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Internal Error", e);
        }
    }

    /**
     * The base URL for notifications as extracted from the {@link Event#getBaseURL() event}; defaults
     * to {@code https://www.sapsailing.com} if no base URL has been provided for the event.
     */
    public static URL getBaseURL(final Event event) {
        final URL result;
        if (event.getBaseURL() == null) {
            result = defaultBaseURL;
        } else {
            result = event.getBaseURL();
        }
        return result;
    }

    public static String createRaceBoardLink(TrackedRace trackedRace, Leaderboard leaderboard, Event event,
            LeaderboardGroup leaderboardGroup, String raceboardMode, Locale locale) {
        RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
        String link;
        try {
            final String eventBaseUrl = getBaseURL(event).toString();
            link = eventBaseUrl
            + (eventBaseUrl.endsWith("/") ? "" : "/")
            + "gwt/RaceBoard.html?"
            + (locale == null ? "" : (locale.toLanguageTag()+"&"))
            + "eventId=" + event.getId() + "&leaderboardName=" + leaderboard.getName()
            + (leaderboardGroup != null ? ("&leaderboardGroupId=" + leaderboardGroup.getId().toString()) : "")
            +"&raceName=" + URLEncoder.encode(raceIdentifier.getRaceName(), "UTF-8") + "&showMapControls=true&regattaName="
            + URLEncoder.encode(raceIdentifier.getRegattaName(), "UTF-8") + "&mode=" + raceboardMode;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Internal error: charset UTF-8 unknown");
        }
        return link;
    }
}
