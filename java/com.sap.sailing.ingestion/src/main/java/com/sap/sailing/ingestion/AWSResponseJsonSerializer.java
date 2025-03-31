package com.sap.sailing.ingestion;

import org.json.simple.JSONObject;

import com.sap.sailing.ingestion.dto.AWSResponseWrapper;
import com.sap.sse.shared.json.JsonSerializer;

public class AWSResponseJsonSerializer<T> implements JsonSerializer<AWSResponseWrapper<T>> {
    public static final String STATUS_CODE = "statusCode";
    public static final String STATUS_DESCRIPTION = "statusDescription";
    public static final String HEADERS = "headers";
    public static final String BODY = "body";

    @Override
    public JSONObject serialize(AWSResponseWrapper<T> object) {
        JSONObject result = new JSONObject();
        result.put(STATUS_CODE, object.getStatusCode());
        result.put(STATUS_DESCRIPTION, object.getStatusDescription());
        result.put(HEADERS, new AWSResponseHttpHeaderJsonSerializer().serialize(object.getHeaders()));
        result.put(BODY, object.getBody());
        return result;
    }
}
