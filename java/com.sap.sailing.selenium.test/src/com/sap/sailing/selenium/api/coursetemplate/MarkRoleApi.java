package com.sap.sailing.selenium.api.coursetemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class MarkRoleApi {

    private static final String MARK_ROLES = "/api/v1/markroles";
    private static final String PARAM_NAME = "name";

    public MarkRole createMarkRole(final ApiContext ctx, final String name) {
        final Map<String, String> formParams = new TreeMap<>();
        formParams.put(PARAM_NAME, name);
        JSONObject result = ctx.post(MARK_ROLES, null, formParams);
        return new MarkRole(result);
    }

    public Iterable<MarkRole> getAllMarkRoles(final ApiContext ctx) {
        JSONArray markPropertiesArray = ctx.get(MARK_ROLES);
        List<MarkRole> result = new ArrayList<>();
        markPropertiesArray.stream().map(o -> (JSONObject) o).map(MarkRole::new).forEach(result::add);
        return result;
    }

    public MarkRole getMarkRole(final ApiContext ctx, final UUID id) {
        JSONObject result = ctx.get(MARK_ROLES + "/" + id.toString());
        return new MarkRole(result);
    }
}
