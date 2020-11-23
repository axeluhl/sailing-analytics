package com.amazonaws.lambda.demo;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Input: " + input);
        context.getLogger().log("Input is of type "+input.getClass().getName());
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("statusCode", 200);
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Cache-Control", "no-store");
        responseMap.put("headers", headers);
        responseMap.put("isBase64Encoded", Boolean.FALSE);
        responseMap.put("body", "<h1>Hello from Lambda!</h1>");
        context.getLogger().log("Response: "+responseMap);
        return responseMap;
    }

}
