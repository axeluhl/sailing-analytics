package com.sap.sailing.selenium.api.event;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class SecurityApi {

    private static final String CREATE_USER_URL = "/api/restsecurity/create_user";
    private static final String GET_USER_URL = "/api/restsecurity/user";
    private static final String SAY_HELLO_URL = "/api/restsecurity/hello";

    public JSONObject createUser(ApiContext ctx, String username, String fullName, String company,
            /* String email, */ String password) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("username", username);
        queryParams.put("fullName", fullName);
        queryParams.put("company", company);
        // queryParams.put("email", email); //if email is provided a validation mail would be sent
        queryParams.put("password", password);
        return ctx.post(CREATE_USER_URL, queryParams);
    }

    public JSONObject getUser(ApiContext ctx, String username) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("username", username);
        return ctx.get(GET_USER_URL, queryParams);
    }

    public JSONObject sayHello(ApiContext ctx) {
        return ctx.get(SAY_HELLO_URL);
    }
}
