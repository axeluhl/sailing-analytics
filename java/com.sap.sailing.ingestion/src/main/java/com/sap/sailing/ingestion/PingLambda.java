package com.sap.sailing.ingestion;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.sap.sailing.ingestion.dto.FixHeaderDTO;

public class PingLambda implements RequestHandler<FixHeaderDTO, String> {
    @Override
    public String handleRequest(FixHeaderDTO input, Context context) {
        final String jsonAsString = new Gson().toJson(input);
        context.getLogger().log(jsonAsString);
        return jsonAsString;
    }
}
