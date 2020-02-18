package com.sap.sailing.selenium.api.coursetemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class MarkTemplateApi {

    private static final String MARK_TEMPLATES = "/api/v1/marktemplates";

    private static final String PARAM_NAME = "name";
    private static final String PARAM_SHORTNAME = "shortName";
    private static final String PARAM_COLOR = "color";
    private static final String PARAM_SHAPE = "shape";
    private static final String PARAM_PATTERN = "pattern";
    private static final String PARAM_MARKTYPE = "markType";

    public MarkTemplate createMarkTemplate(final ApiContext ctx, final String name, final String shortName,
            final String color, final String shape, final String pattern, final String markType) {
        final Map<String, String> formParams = new TreeMap<>();
        formParams.put(PARAM_NAME, name);
        formParams.put(PARAM_SHORTNAME, shortName);
        formParams.put(PARAM_COLOR, color);
        formParams.put(PARAM_SHAPE, shape);
        formParams.put(PARAM_PATTERN, pattern);
        formParams.put(PARAM_MARKTYPE, markType);
        JSONObject result = ctx.post(MARK_TEMPLATES, null, formParams);
        return new MarkTemplate(result);
    }

    public MarkTemplate getMarkTemplate(final ApiContext ctx, final UUID id) {
        JSONObject result = ctx.get(MARK_TEMPLATES + "/" + id.toString());
        return new MarkTemplate(result);
    }

    public Iterable<MarkTemplate> getAllMarkTemplates(final ApiContext ctx) {
        JSONArray markPropertiesArray = ctx.get(MARK_TEMPLATES);
        List<MarkTemplate> result = new ArrayList<>();
        markPropertiesArray.stream().map(o -> (JSONObject) o).map(MarkTemplate::new).forEach(result::add);
        return result;
    }
}
