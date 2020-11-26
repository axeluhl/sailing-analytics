package com.sap.sailing.selenium.api.coursetemplate;

import java.util.Set;

import org.json.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class FreestyleProperties extends JsonWrapper {

    private static final String FIELD_NAME = "name";
    private static final String FIELD_SHORT_NAME = "shortName";
    private static final String FIELD_COLOR = "color";
    private static final String FIELD_SHAPE = "shape";
    private static final String FIELD_PATTERN = "pattern";
    private static final String FIELD_MARK_TYPE = "markType";
    private static final String FIELD_TAG = "tags";

    public FreestyleProperties(final JSONObject json) {
        super(json);
    }

    public FreestyleProperties(final String name, final String shortName, final String color, final String shape,
            final String pattern, final String markType, Set<String> tags) {
        super(new JSONObject());
        getJson().put(FIELD_NAME, name);
        getJson().put(FIELD_SHORT_NAME, shortName);
        getJson().put(FIELD_COLOR, color);
        getJson().put(FIELD_SHAPE, shape);
        getJson().put(FIELD_PATTERN, pattern);
        getJson().put(FIELD_MARK_TYPE, markType);
        JSONArray jsonTags = new JSONArray(tags);
        getJson().put(FIELD_TAG, jsonTags);
    }

    public String getName() {
        return (String) getJson().get(FIELD_NAME);
    }

    public String getShortName() {
        return (String) getJson().get(FIELD_SHORT_NAME);
    }

}
