package com.sap.sailing.selenium.api.event;

import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class RegattaApi {

    private static final String REGATTAS = "/api/v1/regattas";
    private static final String LIST_REGATTA_RACES = "/races";
    private static final String COMPETITOR_CREATE_AND_ADD_WITH_BOAT = "/competitors/createandadd";

    public JSONObject getRegatta(ApiContext ctx, String regattaName) {
        return ctx.get(REGATTAS + "/" + regattaName);
    }

    public JSONObject getRegattaRaces(ApiContext ctx, String regattaName) {
        return ctx.get(REGATTAS + "/" + regattaName + LIST_REGATTA_RACES);
    }

    public JSONObject createAndAddCompetitor(ApiContext ctx, String regattaName, String boatclass,
            String competitorEmail, String competitorName, String nationalityIOC) {
        String url = REGATTAS + "/" + regattaName + COMPETITOR_CREATE_AND_ADD_WITH_BOAT;
        Map<String, String> queryparams = new TreeMap<>();
        queryparams.put("boatclass", boatclass);
        queryparams.put("competitorEmail", competitorEmail);
        queryparams.put("competitorName", competitorName);
        queryparams.put("nationalityIOC", nationalityIOC);
        return ctx.post(url, queryparams);
    }
}
