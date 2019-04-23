package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAnonymousApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage.goToPage;

import java.util.UUID;

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

public class ClosedRegattaTest extends AbstractSeleniumTest {

    private ApiContext adminSecurityCtx;
    private ApiContext ownerCtx;
    // private ApiContext registeredCompetitor;
    private ApiContext unregisteredCompetitor;

    private SecurityApi securityApi = new SecurityApi();
    private EventApi eventApi = new EventApi();
    private RegattaApi regattaApi = new RegattaApi();

    private static final String EVENT_NAME = "Super exclusive regatta - invited competitors only";
    private static final String BOAT_CLASS = "GC 32";

    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        adminSecurityCtx = createApiContext(getContextRoot(), SECURITY_CONTEXT, "admin", "admin");
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        ownerCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "donald", "daisy0815");
        unregisteredCompetitor = createAnonymousApiContext(getContextRoot(), SERVER_CONTEXT);
        AdminConsolePage adminConsole = goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLcalServerPanel().setSelfServiceServer(true);
    }

    @Test
    public void simpleTest() {
        final Event event = eventApi.createEvent(ownerCtx, EVENT_NAME, BOAT_CLASS, CompetitorRegistrationType.CLOSED,
                "Some special place");
        final String registrationLinkSecret = event.getSecret();
        regattaApi.createAndAddCompetitor(ownerCtx, EVENT_NAME, BOAT_CLASS, "", "Donald Duck", "USA");
        regattaApi.createAndAddCompetitorWithSecret(unregisteredCompetitor, EVENT_NAME, BOAT_CLASS, "", "Mickey Mouse",
                "USA", registrationLinkSecret, UUID.randomUUID());
    }
}
