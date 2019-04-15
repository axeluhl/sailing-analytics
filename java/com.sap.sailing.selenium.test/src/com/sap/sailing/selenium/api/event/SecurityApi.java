package com.sap.sailing.selenium.api.event;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class SecurityApi {

    private static final String CREATE_USER_URL = "/api/restsecurity/create_user";
    private static final String GET_USER_URL = "/api/restsecurity/user";
    private static final String SAY_HELLO_URL = "/api/restsecurity/hello";

    public AccessToken createUser(ApiContext ctx, String username, String fullName, String company,
            /* String email, */ String password) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("username", username);
        queryParams.put("fullName", fullName);
        queryParams.put("company", company);
        // queryParams.put("email", email); //if email is provided a validation mail would be sent
        queryParams.put("password", password);
        AccessToken accessToken = new AccessToken(ctx.post(CREATE_USER_URL, queryParams));
        return accessToken;
    }

    public User getUser(ApiContext ctx, String username) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("username", username);
        User user = new User(ctx.get(GET_USER_URL, queryParams));
        return user;
    }

    public Hello sayHello(ApiContext ctx) {
        Hello hello = new Hello(ctx.get(SAY_HELLO_URL));
        return hello;
    }

    public class AccessToken extends JsonWrapper {

        public AccessToken(JSONObject json) {
            super(json);
        }

        public String getUsername() {
            return get("username");
        }

        public String getAccessToken() {
            return get("access_token");
        }
    }

    public class User extends JsonWrapper {

        public User(JSONObject json) {
            super(json);
        }

        public String getUsername() {
            return get("username");
        }

        public String getFullName() {
            return get("fullname");
        }
    }

    public class Hello extends JsonWrapper {

        public Hello(JSONObject json) {
            super(json);
        }

        public String getPrincipal() {
            return get("principal");
        }

        public Boolean isAuthenticated() {
            return get("authenticated");
        }
    }
}
