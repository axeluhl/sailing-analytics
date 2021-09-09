package com.sap.sailing.gwt.ui.client;

import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class EntryPointLinkFactory extends AbstractEntryPointLinkFactory {
    public static final String LEADERBOARD_PATH = "/gwt/Leaderboard.html";
    public static final String AUTOPLAY_PATH = "/gwt/AutoPlay.html";
    private static final String HOME_PATH = "/gwt/Home.html";

    public static String createSimulatorLink(final Map<String, String> parameters) {
        return createEntryPointLink("/gwt/Simulator.html", parameters);
    }

    public static String createLeaderboardTabLink(final String eventId, final String regattaId) {
        return createEventRegattaTabLink(eventId, regattaId, "leaderboard");
    }

    public static String createRacesTabLink(final String eventId, final String leaderboardName) {
        return createEventRegattaTabLink(eventId, leaderboardName, "races");
    }

    private static String createEventRegattaTabLink(final String eventId, final String regattaId, final String tabName) {
        return createEntryPointLink(
                HOME_PATH, "/regatta/" + tabName + "/:eventId=" + eventId + "&regattaId=" + regattaId,
                emptyMap());
    }

    public static String createLeaderboardGroupLink(final Map<String, String> parameters) {
        return createEntryPointLink("/gwt/Spectator.html", parameters);
    }

    public static String createEventPlaceLink(final String eventId, final Map<String, String> parameters) {
        return createEntryPointLink(HOME_PATH, "/event/:eventId="+eventId, parameters);
    }

    public static String createDashboardLink(final Map<String, String> parameters) {
        return createEntryPointLink("/dashboards/RibDashboard.html", parameters);
    }

    public static String createPairingListLink(final Map<String, String> parameters) {
        return createEntryPointLink("/gwt/PairingList.html", parameters);
    }

    public static String createSubscriptionPageLink(final Set<SubscriptionPlanDTO> highlightedPlans) {
        final StringJoiner joiner = new StringJoiner("&", PlaceTokenPrefixes.Subscription + ":", "");
        highlightedPlans.stream().map(SubscriptionPlanDTO::getId).forEach(id -> joiner.add("highlight=" + id));
        return createEntryPointLink(HOME_PATH, joiner.toString(), emptyMap());
    }
}
