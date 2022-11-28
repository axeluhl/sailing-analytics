package com.sap.sailing.ingestion;

import org.json.simple.JSONObject;

import com.sap.sailing.ingestion.dto.AWSResponseHttpHeader;
import com.sap.sse.shared.json.JsonSerializer;

public class AWSResponseHttpHeaderJsonSerializer implements JsonSerializer<AWSResponseHttpHeader> {
    public static final String CONTENT_TYPE = "Content-Type";

    @Override
    public JSONObject serialize(AWSResponseHttpHeader object) {
        JSONObject result = new JSONObject();
        result.put(CONTENT_TYPE, object.getContentType());
        return result;
    }
}
