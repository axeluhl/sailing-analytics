package com.sap.sailing.gwt.home.desktop.places.qrcode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sse.common.Util.Pair;

public class QRCodePlace extends AbstractBasePlace {

    private static final String PARAM_EVENT_ID = "event_id";
    private static final String PARAM_LEADERBOARD_NAME = "leaderboard_name";
    private static final String PARAM_COMPETITOR_ID = "competitor_id";
    private static final String PARAM_CHECKIN_URL = "checkinUrl";
    private static final String PARAM_MODE = "mode";

    private UUID eventId;
    private UUID competitorId;
    private String leaderboardName;
    private String checkInUrl;
    private InvitationMode mode;
    private String rawCheckInUrl;

    public QRCodePlace(UUID eventId, UUID competitorId, String leaderboardName, String checkInUrl) {
        this.eventId = eventId;
        this.competitorId = competitorId;
        this.leaderboardName = leaderboardName;
        this.checkInUrl = checkInUrl;
    }

    public enum InvitationMode {
        COMPETITOR,
        BOUY_TENDER
    }

    public QRCodePlace(String token) {
        super(token);
        try {
            mode = InvitationMode.valueOf(getParameter(PARAM_MODE));
            rawCheckInUrl = Window.Location.getParameter(PARAM_CHECKIN_URL);
            if (rawCheckInUrl != null) {
                checkInUrl = decodeUrl(rawCheckInUrl);
                parseUrl(checkInUrl);

                if (leaderboardName == null) {
                    GWT.log("No parameter " + PARAM_LEADERBOARD_NAME + " found!");
                }
                if (competitorId == null && mode == InvitationMode.COMPETITOR) {
                    GWT.log("No parameter " + PARAM_COMPETITOR_ID + " found!");
                }
                if (competitorId != null && mode == InvitationMode.BOUY_TENDER) {
                    GWT.log("Found parameter " + PARAM_COMPETITOR_ID + " but is not required!");
                }
                if (eventId == null) {
                    GWT.log("No parameter " + PARAM_EVENT_ID + " found!");
                }
            } else {
                GWT.log("No parameter " + PARAM_CHECKIN_URL + " found!");
            }
        } catch (Exception e) {
            GWT.log("No parameter " + PARAM_MODE + " found, or value not valid");
        }
    }

    private native String decodeUrl(String url)/*-{
        return decodeURIComponent(url)
    }-*/;

    private void parseUrl(String checkInUrl) {
        Iterable<Pair<String, String>> parameters = parseUrlParameters(checkInUrl);
        for (Pair<String, String> parameter : parameters) {
            if (PARAM_EVENT_ID.equals(parameter.getA())) {
                try {
                    eventId = UUID.fromString(parameter.getB());
                } catch (IllegalArgumentException e) {
                    GWT.log("Invalid event_id");
                    eventId = null;
                }
            } else if (PARAM_COMPETITOR_ID.equals(parameter.getA())) {
                try {
                    competitorId = UUID.fromString(parameter.getB());
                } catch (IllegalArgumentException e) {
                    GWT.log("Invalid competitor_id");
                    competitorId = null;
                }
            } else if (PARAM_LEADERBOARD_NAME.equals(parameter.getA())) {
                leaderboardName = parameter.getB().replace("+", " ");
            }
        }
    }

    private Iterable<Pair<String, String>> parseUrlParameters(String checkInUrl) {
        String[] urlArguments = checkInUrl.split("\\?");
        Collection<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();

        if (urlArguments.length < 2) {
            GWT.log("No parameters found!");
        } else {
            String[] urlParams = urlArguments[1].split("&");
            for (String urlParam : urlParams) {
                String[] param = urlParam.split("=");
                Pair<String, String> pair = new Pair<String, String>(param[0], param[1]);
                pairs.add(pair);
            }
        }
        return pairs;
    }

    public InvitationMode getMode() {
        return mode;
    }

    public String getEncodedCheckInUrl() {
        return rawCheckInUrl;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getCompetitorId() {
        return competitorId;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public static class Tokenizer implements PlaceTokenizer<QRCodePlace> {
        @Override
        public String getToken(QRCodePlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public QRCodePlace getPlace(String token) {
            return new QRCodePlace(token);
        }
    }
}
