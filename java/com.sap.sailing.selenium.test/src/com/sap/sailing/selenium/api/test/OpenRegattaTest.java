package com.sap.sailing.selenium.api.test;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
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

    @Test
    public void simpleTest() {
        Event event = eventApi.createEvent(ownerCtx, EVENT_NAME, BOAT_CLASS,
                CompetitorRegistrationType.OPEN_UNMODERATED, "Duckburg Harbour");
        String registrationLinkSecret = event.getSecret();
        regattaApi.createAndAddCompetitor(ownerCtx, EVENT_NAME, BOAT_CLASS, "", "Donald Duck", "USA");
        regattaApi.createAndAddCompetitorWithSecret(sailorCtx, EVENT_NAME, BOAT_CLASS, "", "Mickey Mouse", "USA",
                registrationLinkSecret, "00000000-0000-0000-0000-000000000001");
    }

}
