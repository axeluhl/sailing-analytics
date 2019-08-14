package com.sap.sailing.selenium.api.coursetemplate;

import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class MarkPropertiesApi {

    private static final String MARK_PROPERTIES = "/api/v1/markproperties";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_SHORTNAME = "shortName";
    private static final String PARAM_DEVICEUUID = "deviceUuid";

    public MarkProperties createMarkProperties(final ApiContext ctx, final String name, final String shortName,
            final String deviceUuid) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(PARAM_NAME, "test");
        queryParams.put(PARAM_SHORTNAME, "test");
        queryParams.put(PARAM_DEVICEUUID, deviceUuid);
        JSONObject result = ctx.post(MARK_PROPERTIES, queryParams);
        return new MarkProperties(result);
    }
}
