package com.sap.sailing.ingestion;

import org.json.simple.JSONObject;

import com.sap.sailing.ingestion.dto.AWSRequestWrapper;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

public class AWSRequestJsonDeserializer implements JsonDeserializer<AWSRequestWrapper> {
    public static final String HTTP_METHOD = "httpMethod";
    public static final String BODY = "body";
    public static final String IS_BASE64_ENCODED = "isBase64Encoded";

    @Override
    public AWSRequestWrapper deserialize(JSONObject object) throws JsonDeserializationException {
        String httpMethod = String.valueOf(object.get(HTTP_METHOD));
        String body = String.valueOf(object.get(BODY));
        Boolean isBase64Encoded = Boolean.parseBoolean(String.valueOf(object.get(IS_BASE64_ENCODED)));
        AWSRequestWrapper request = new AWSRequestWrapper(httpMethod, body, isBase64Encoded);
        return request;
    }
}
