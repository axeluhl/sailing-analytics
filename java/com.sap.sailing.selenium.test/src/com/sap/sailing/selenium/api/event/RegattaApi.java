package com.sap.sailing.selenium.api.event;

import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class RegattaApi {

    private static final String REGATTAS = "/api/v1/regattas";
    private static final String LIST_REGATTA_RACES = "/races";
    private static final String COMPETITOR_CREATE_AND_ADD_WITH_BOAT = "/competitors/createandadd";
    private static final String ADD_RACE_COLUMN_URL = "/addracecolumns";

    public JSONObject getRegatta(ApiContext ctx, String regattaName) {
        return ctx.get(REGATTAS + "/" + regattaName);
    }

    public JSONObject getRegattaRaces(ApiContext ctx, String regattaName) {
        return ctx.get(REGATTAS + "/" + regattaName + LIST_REGATTA_RACES);
    }

    public JSONObject createAndAddCompetitor(ApiContext ctx, String regattaName, String boatclass,
            String competitorEmail, String competitorName, String nationalityIOC) {
        String url = REGATTAS + "/" + regattaName + COMPETITOR_CREATE_AND_ADD_WITH_BOAT;
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("boatclass", boatclass);
        queryParams.put("competitorEmail", competitorEmail);
        queryParams.put("competitorName", competitorName);
        queryParams.put("nationalityIOC", nationalityIOC);
        return ctx.post(url, queryParams);
    }
    
    public JSONObject createAndAddCompetitorWithSecret(ApiContext ctx, String regattaName, String boatclass,
            String competitorEmail, String competitorName, String nationalityIOC, String secret, String deviceUuid) {
        String url = REGATTAS + "/" + regattaName + COMPETITOR_CREATE_AND_ADD_WITH_BOAT;
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("boatclass", boatclass);
        queryParams.put("competitorEmail", competitorEmail);
        queryParams.put("competitorName", competitorName);
        queryParams.put("nationalityIOC", nationalityIOC);
        queryParams.put("secret", secret);
        queryParams.put("deviceUuid", deviceUuid);
        return ctx.post(url, queryParams);
    }
    
    public JSONArray addRaceColumn(ApiContext ctx, String regattaName, String prefix, Integer numberOfRaces) {
        String url = REGATTAS + "/" + regattaName + ADD_RACE_COLUMN_URL;
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("prefix", prefix);
        queryParams.put("numberOfRaces", numberOfRaces != null ? numberOfRaces.toString() : null);
        return ctx.post(url, queryParams);
        
    }
}
