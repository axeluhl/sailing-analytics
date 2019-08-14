package com.sap.sailing.selenium.api.coursetemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.json.simple.JSONArray;
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

    public MarkProperties getMarkProperties(final ApiContext ctx, final UUID id) {
        JSONObject result = ctx.get(MARK_PROPERTIES + "/" + id.toString());
        return new MarkProperties(result);
    }

    public Iterable<MarkProperties> getAllMarkProperties(final ApiContext ctx, final Iterable<String> tags) {
        // FIXME: multiple query parameters with the same key sould be passed but cannot be put into Map<String,
        // String>. Should use Map<String, Iterator<String>>. Will be fixed in bug4942.
        final Map<String, String> queryParams = new TreeMap<>();
        for (String tag : tags) {
            queryParams.put("tag", tag);
        }
        JSONArray markPropertiesArray = ctx.get(MARK_PROPERTIES, queryParams);
        List<MarkProperties> result = new ArrayList<>();
        markPropertiesArray.stream().map(o -> (JSONObject) o).map(MarkProperties::new).forEach(result::add);
        return result;
    }

    public void deleteMarkProperties(final ApiContext ctx, final UUID id) {
        ctx.delete(MARK_PROPERTIES + "/" + id.toString());
    }
}
