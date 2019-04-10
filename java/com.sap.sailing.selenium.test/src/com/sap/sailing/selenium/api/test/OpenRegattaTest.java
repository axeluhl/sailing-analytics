package com.sap.sailing.selenium.api.test;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.RegattaApi;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class OpenRegattaTest extends AbstractSeleniumTest {

    private ApiContext adminSecurityCtx;
    private ApiContext ownerCtx;
    private ApiContext sailorCtx;

    private SecurityApi securityApi = new SecurityApi();
    private EventApi eventApi = new EventApi();
    private RegattaApi regattaApi = new RegattaApi();

    private static final String EVENT_NAME = "Duckburg 2019 Everybody's Regatta";
    private static final String BOAT_CLASS = "49er";

    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        adminSecurityCtx = ApiContext.createApiContext(getContextRoot(), ApiContext.SECURITY_CONTEXT, "admin", "admin");
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        ownerCtx = ApiContext.createApiContext(getContextRoot(), ApiContext.SERVER_CONTEXT, "donald", "daisy0815");
        sailorCtx = ApiContext.createAnonymousApiContext(getContextRoot(), ApiContext.SERVER_CONTEXT);
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLcalServerPanel().setSelfServiceServer(true);
    }

    // Event erzeugen
    // Competitor anlegen owner
    // Competitor anlegen
    // Start Race
    // GPS (n*)
    // End Race

    @Test
    public void simpleTest() {
        JSONObject event = eventApi.createEvent(ownerCtx, EVENT_NAME, BOAT_CLASS, "OPEN_UNMODERATED",
                "Duckburg Harbour");
        String registrationLinkSecret = (String) event.get("registrationSecret");
        regattaApi.createAndAddCompetitor(ownerCtx, EVENT_NAME, BOAT_CLASS, "", "Donald Duck", "USA");
        regattaApi.createAndAddCompetitorWithSecret(sailorCtx, EVENT_NAME, BOAT_CLASS, "", "Mickey Mouse", "USA",
                registrationLinkSecret, "00000000-0000-0000-0000-000000000001");
    }

}
