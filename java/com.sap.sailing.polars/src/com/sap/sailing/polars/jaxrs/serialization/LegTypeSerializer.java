package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class LegTypeSerializer implements JsonSerializer<LegType> {

    public static final String FIELD_VALUE = "value";

    @Override
    public JSONObject serialize(LegType object) {
        JSONObject keyJSON = new JSONObject();

        keyJSON.put(FIELD_VALUE, object.toString());

        return keyJSON;
    }

}
