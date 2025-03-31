package com.sap.sailing.selenium.api.core;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openqa.selenium.Cookie;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

public class Authenticator {

    private static final String SECURITY_CONTEXT = "security";
    private static final String OBTAIN_ACCESS_TOKEN_URL = "/api/restsecurity/access_token";

    private final Client client;
    private final WebResource securityResource;

    public Authenticator(String contextRoot) {
        client = Client.create();
        securityResource = client.resource(contextRoot + SECURITY_CONTEXT);
    }

    // TODO
    public Cookie authForCookie(String username, String password) {
        return null;
    }

    public String authForToken(String username, String password) {
        Form form = new Form();
        form.putSingle("username", username);
        form.putSingle("password", password);
        String tokenJson = securityResource.path(OBTAIN_ACCESS_TOKEN_URL).entity(form).post(String.class);
        JSONObject obj = (JSONObject) JSONValue.parse(tokenJson);
        String token = (String) obj.get("access_token");
        return token;
    }

}
