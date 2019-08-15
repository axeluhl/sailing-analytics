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
    private static final String PARAM_TAG = "tag";
    private static final String PARAM_FIXED_POSITION_LATDEG = "latDeg";
    private static final String PARAM_FIXED_POSITION_LONDEG = "lonDeg";

    public MarkProperties createMarkProperties(final ApiContext ctx, final String name, final String shortName,
            final String deviceUuid, final String color, final String shape, final String pattern,
            final String markType, final Iterable<String> tags, final Double latDeg, final Double lonDeg) {
        final Map<String, String> formParams = new TreeMap<>();
        formParams.put(PARAM_NAME, name);
        formParams.put(PARAM_SHORTNAME, shortName);
        if (deviceUuid != null) {
            formParams.put(PARAM_DEVICEUUID, deviceUuid);
        }
        formParams.put(PARAM_COLOR, color);
        formParams.put(PARAM_SHAPE, shape);
        formParams.put(PARAM_PATTERN, pattern);
        formParams.put(PARAM_MARKTYPE, markType);
        formParams.put(PARAM_FIXED_POSITION_LATDEG, latDeg != null ? latDeg.toString() : null);
        formParams.put(PARAM_FIXED_POSITION_LONDEG, latDeg != null ? lonDeg.toString() : null);
        for (String tag : tags) {
            formParams.put(PARAM_TAG, tag);
        }
        JSONObject result = ctx.post(MARK_PROPERTIES, null, formParams);
        return new MarkProperties(result);
    }

    public MarkProperties updateMarkProperties(final ApiContext ctx, final UUID id, final UUID deviceUuid,
            final Double latDeg, final Double lonDeg) {
        final Map<String, String> formParams = new TreeMap<>();
        if (deviceUuid != null) {
            formParams.put(PARAM_DEVICEUUID, deviceUuid.toString());
        }
        formParams.put(PARAM_FIXED_POSITION_LATDEG, latDeg != null ? latDeg.toString() : null);
        formParams.put(PARAM_FIXED_POSITION_LONDEG, latDeg != null ? lonDeg.toString() : null);

        // FIXME: add ApiContext.put with formParams. Do it in bug4942.
        JSONObject result = null; // ctx.put(MARK_PROPERTIES + "/" + id.toString(), null, formParams);
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
            queryParams.put(PARAM_TAG, tag);
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
