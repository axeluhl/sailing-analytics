package com.sap.sailing.selenium.api.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.AbstractApiTest;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.SecurityApi;

public class SecurityApiTest extends AbstractApiTest {

    private static final String USERNAME = "max";
    private static final String USERNAME_FULL = "Max Mustermann";

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testCreateAndGetUser() {
        ApiContext adminCtx = ApiContext.createApiContext(getContextRoot(), SECURITY_CONTEXT, "admin", "admin");

        SecurityApi securityApi = new SecurityApi();
        JSONObject createUserResponse = securityApi.createUser(adminCtx, "max", USERNAME_FULL, null, "start123");

        assertEquals("Responded username of createUser is different!", USERNAME, createUserResponse.get("username"));
        assertNotNull("Token is missing in reponse!", createUserResponse.get("access_token"));

        JSONObject getUserResponse = securityApi.getUser(adminCtx, USERNAME);
        assertEquals("Responded username of getUser is different!", USERNAME, getUserResponse.get("username"));
    }

    @Test
    public void testSayHello() {
        ApiContext adminCtx = ApiContext.createApiContext(getContextRoot(), SECURITY_CONTEXT, "admin", "admin");
        SecurityApi securityApi = new SecurityApi();

        JSONObject helloResponse = securityApi.sayHello(adminCtx);
        assertEquals("Responded principal of hello is different!", "admin", helloResponse.get("principal"));
        assertEquals("Responded authenticated of hello is different!", true, helloResponse.get("authenticated"));
    }
}
