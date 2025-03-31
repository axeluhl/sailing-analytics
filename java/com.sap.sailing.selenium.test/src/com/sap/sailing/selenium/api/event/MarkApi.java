package com.sap.sailing.selenium.api.event;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class MarkApi {

    private static final String URL_ADD_MARK_TO_REGATTA = "api/v1/mark/addMarkToRegatta";
    private static final String URL_ADD_MARK_FIX = "api/v1/mark/addMarkFix";
    private static final String URL_REVOKE_MARK_ON_REGATTA = "api/v1/mark/revokeMarkOnRegatta";

    private static final String ATTRIBUTE_REGATTA_NAME = "regattaName";
    private static final String ATTRIBUTE_LEADERBOARD_NAME = "leaderboardName";
    private static final String ATTRIBUTE_MARK_NAME = "markName";
    private static final String ATTRIBUTE_MARK_ID = "markId";
    private static final String ATTRIBUTE_RACECOLUMN_NAME = "raceColumnName";
    private static final String ATTRIBUTE_FLEET_NAME = "fleetName";
    private static final String ATTRIBUTE_LONGITUDE = "lonDeg";
    private static final String ATTRIBUTE_LATITUDE = "latDeg";
    private static final String ATTRIBUTE_TIME = "timeMillis";
    private static final String ATTRIBUTE_ORIGINATING_MARK_TEMPLATE_ID = "originatingMarkTemplateId";
    private static final String ATTRIBUTE_ORIGINATING_MARK_PROPERTIES_ID = "originatingMarkPropertiesId";

    public Mark addMarkToRegatta(final ApiContext ctx, final String regattaName, final String markName) {
        final JSONObject json = new JSONObject();
        json.put(ATTRIBUTE_REGATTA_NAME, regattaName);
        json.put(ATTRIBUTE_MARK_NAME, markName);
        return new Mark(ctx.post(URL_ADD_MARK_TO_REGATTA, null, json));
    }

    public JSONObject addMarkFix(final ApiContext ctx, final String leaderBoardName, final String raceColumnName,
            final String fleetName, final UUID markId, final UUID markTemplateId, final UUID markPropertiesId,
            double longitude, double latitude, long timeMillis) {
        final JSONObject json = new JSONObject();
        json.put(ATTRIBUTE_LEADERBOARD_NAME, leaderBoardName);
        json.put(ATTRIBUTE_RACECOLUMN_NAME, raceColumnName);
        json.put(ATTRIBUTE_FLEET_NAME, fleetName);
        json.put(ATTRIBUTE_MARK_ID, markId.toString());
        json.put(ATTRIBUTE_LONGITUDE, Double.toString(longitude));
        json.put(ATTRIBUTE_LATITUDE, Double.toString(latitude));
        json.put(ATTRIBUTE_TIME, Long.toString(timeMillis));
        json.put(ATTRIBUTE_ORIGINATING_MARK_TEMPLATE_ID, markTemplateId != null ? markTemplateId.toString() : null);
        json.put(ATTRIBUTE_ORIGINATING_MARK_PROPERTIES_ID, markPropertiesId != null ? markPropertiesId.toString() : null);
        return ctx.post(URL_ADD_MARK_FIX, null, json);
    }
    
    public JSONObject revokeMarkOnRegatta(final ApiContext ctx, final String regattaName, final String raceColumnName,
            final String fleetName, final UUID markId) {
        final JSONObject json = new JSONObject();
        json.put(ATTRIBUTE_REGATTA_NAME, regattaName);
        json.put(ATTRIBUTE_RACECOLUMN_NAME, raceColumnName);
        json.put(ATTRIBUTE_FLEET_NAME, fleetName);
        json.put(ATTRIBUTE_MARK_ID, markId.toString());
        return ctx.post(URL_REVOKE_MARK_ON_REGATTA, null, json);
    }

}
