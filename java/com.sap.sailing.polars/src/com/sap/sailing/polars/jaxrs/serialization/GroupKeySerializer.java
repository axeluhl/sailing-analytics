package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.datamining.shared.GroupKey;

public class GroupKeySerializer implements JsonSerializer<GroupKey> {

    public static final String FIELD_GROUP_KEY = "groupKey";

    @Override
    public JSONObject serialize(GroupKey object) {
        JSONObject groupKeyJSON = new JSONObject();

        groupKeyJSON.put(FIELD_GROUP_KEY, object.asString());

        return groupKeyJSON;
    }

}
