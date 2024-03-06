package com.sap.sailing.ingestion;

import java.io.IOException;
import java.io.InputStream;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.ingestion.dto.AWSRequestWrapper;
import com.sap.sailing.ingestion.dto.AWSResponseWrapper;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;
import com.sap.sse.shared.json.JsonSerializer;

import software.amazon.awssdk.utils.IoUtils;

public class AWSInOutHandler {
    private final JsonDeserializer<AWSRequestWrapper> awsRequestDeserializer = new AWSRequestJsonDeserializer();
    private final JsonSerializer<AWSResponseWrapper<String>> awsResponseSerializer = new AWSResponseJsonSerializer<String>();

    public JSONObject parseInputToJson(InputStream inputAsStream)
            throws IOException, JsonDeserializationException, ParseException {
        final byte[] streamAsBytes = IoUtils.toByteArray(inputAsStream);
        final String s = new String(streamAsBytes);
        final Object awsRequestObject = JSONValue.parseWithException(s);
        final JSONObject awsRequestJson = Helpers.toJSONObjectSafe(awsRequestObject);
        final AWSRequestWrapper requestWrapped = awsRequestDeserializer.deserialize(awsRequestJson);
        final Object requestBody = JSONValue.parseWithException(requestWrapped.getBody());
        final JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
        return requestObject;
    }

    public JSONObject createJsonResponse(String response) {
        final AWSResponseWrapper<String> awsResponseString = AWSResponseWrapper.successResponseAsJson(response);
        final JSONObject responseObject = awsResponseSerializer.serialize(awsResponseString);
        return responseObject;
    }
}
