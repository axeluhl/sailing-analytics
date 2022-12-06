package com.sap.sailing.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.sap.sse.shared.json.JsonDeserializationException;

public class PingLambda implements RequestStreamHandler {
    private final AWSInOutHandler awsInOut = new AWSInOutHandler();

    @Override
    public void handleRequest(final InputStream inputAsStream, final OutputStream outputAsStream, final Context context) {
        try {
            final String jsonAsString = awsInOut.parseInputToJson(inputAsStream).toJSONString();
            context.getLogger().log(jsonAsString);
            String successResponse = awsInOut.createJsonResponse(jsonAsString).toJSONString();
            outputAsStream.write(successResponse.getBytes());
        } catch (ParseException | JsonDeserializationException e) {
            context.getLogger().log("Exception trying to deserialize JSON input: " + e.getMessage());
        } catch (IOException e) {
            context.getLogger().log(e.getMessage());
        }
    }
}
