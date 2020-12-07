package com.sap.sailing.selenium.api.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONAware;

import com.sap.sailing.selenium.api.core.ApiContext;

public class TrackedRacesListApi {

    public JSONAware getRaces(ApiContext ctx, boolean transitive, List<String> eventIds, String pred ) {
        Map<String,String> queryParams = new HashMap<>();
        JSONAware json = ctx.get("/api/v1/trackedRaces/getRaces", queryParams);
        json.toJSONString();
        return json;
    }
}
