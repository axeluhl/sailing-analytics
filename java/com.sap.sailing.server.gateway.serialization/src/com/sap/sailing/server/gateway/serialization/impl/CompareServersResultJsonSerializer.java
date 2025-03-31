package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.interfaces.CompareServersResult;
import com.sap.sse.shared.json.JsonSerializer;

public class CompareServersResultJsonSerializer implements JsonSerializer<CompareServersResult> {

    @Override
    public JSONObject serialize(CompareServersResult object) {
        final JSONObject result = new JSONObject();
        final JSONArray aDiffs = new JSONArray();
        object.getADiffs().forEach(aDiffs::add);
        final JSONArray bDiffs = new JSONArray();
        object.getBDiffs().forEach(bDiffs::add);
        result.put(object.getServerA(), aDiffs);
        result.put(object.getServerB(), bDiffs);
        return result;
    }

}
