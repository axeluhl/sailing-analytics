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
    private static final String PARAM_COLOR = "color";
    private static final String PARAM_SHAPE = "shape";
    private static final String PARAM_PATTERN = "pattern";
    private static final String PARAM_MARKTYPE = "markType";

    public MarkProperties createMarkProperties(final ApiContext ctx, final String name, final String shortName,
            final String deviceUuid, final String color, final String shape, final String pattern,
            final String markType) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(PARAM_NAME, name);
        queryParams.put(PARAM_SHORTNAME, shortName);
        if (deviceUuid != null) {
            queryParams.put(PARAM_DEVICEUUID, deviceUuid);
        }
        queryParams.put(PARAM_COLOR, color);
        queryParams.put(PARAM_SHAPE, shape);
        queryParams.put(PARAM_PATTERN, pattern);
        queryParams.put(PARAM_MARKTYPE, markType);
        JSONObject result = ctx.post(MARK_PROPERTIES, queryParams);
        return new MarkProperties(result);
    }
}
