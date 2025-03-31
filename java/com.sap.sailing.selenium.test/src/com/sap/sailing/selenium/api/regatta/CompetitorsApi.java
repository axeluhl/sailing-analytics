package com.sap.sailing.selenium.api.regatta;

import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class CompetitorsApi {

    private static final String COMPETITORS = "/api/v1/competitors";

    public Competitor getCompetitor(ApiContext ctx, UUID competitorId) {
        return new Competitor(ctx.get(COMPETITORS + "/" + competitorId.toString()));
    }

    public Competitor updateCompetitor(ApiContext ctx, UUID competitorId, Map<String, Object> valuesToUpdate) {
        JSONObject body = new JSONObject();
        body.putAll(valuesToUpdate);
        return new Competitor(ctx.put(COMPETITORS + "/" + competitorId.toString(), null, body));
    }
}
