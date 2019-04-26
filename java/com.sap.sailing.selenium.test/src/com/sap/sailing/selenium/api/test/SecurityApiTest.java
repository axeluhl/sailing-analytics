package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.event.SecurityApi.AccessToken;
import com.sap.sailing.selenium.api.event.SecurityApi.Hello;
import com.sap.sailing.selenium.api.event.SecurityApi.User;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class SecurityApiTest extends AbstractSeleniumTest {

    private static final String USERNAME = "max";
    private static final String USERNAME_FULL = "Max Mustermann";

    private final SecurityApi securityApi = new SecurityApi();

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testCreateAndGetUser() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);

        final AccessToken createUserResponse = securityApi.createUser(adminCtx, "max", USERNAME_FULL, null, "start123");

        assertEquals("Responded username of createUser is different!", USERNAME, createUserResponse.getUsername());
        assertNotNull("Token is missing in reponse!", createUserResponse.getAccessToken());

        User getUserResponse = securityApi.getUser(adminCtx, USERNAME);
        assertEquals("Responded username of getUser is different!", USERNAME, getUserResponse.getUsername());
    }

    @Test
    public void testSayHello() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);

        final Hello hello = securityApi.sayHello(adminCtx);
        assertEquals("Responded principal of hello is different!", "admin", hello.getPrincipal());
        assertEquals("Responded authenticated of hello is different!", true, hello.isAuthenticated());
    }
}
