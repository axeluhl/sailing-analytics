package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createAnonymousApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage.goToPage;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
import com.sap.sailing.selenium.api.event.RegattaApi;
import com.sap.sailing.selenium.api.event.RegattaApi.Competitor;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class ClosedRegattaTest extends AbstractSeleniumTest {

    private ApiContext ownerCtx;
    private ApiContext unregisteredCtx;

    private final SecurityApi securityApi = new SecurityApi();
    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();

    private static final String EVENT_NAME = "Super exclusive regatta - invited competitors only";
    private static final String BOAT_CLASS = "GC 32";

    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        final ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        ownerCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "donald", "daisy0815");
        unregisteredCtx = createAnonymousApiContext(getContextRoot(), SERVER_CONTEXT);
        final AdminConsolePage adminConsole = goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLocalServerPanel().setSelfServiceServer(true);
    }

    @Test
    public void testRegisterOwnerSucceededSelfRegistrationFailed() {
        final Event event = eventApi.createEvent(ownerCtx, EVENT_NAME, BOAT_CLASS, CompetitorRegistrationType.CLOSED,
                "Some special place");
        final String registrationLinkSecret = event.getSecret();
        final Competitor owner = regattaApi.createAndAddCompetitor(ownerCtx, EVENT_NAME, BOAT_CLASS, "", "Donald Duck",
                "USA");
        assertNotNull("Owner should be able to register for a closed regatta!", owner);
        try {
            regattaApi.createAndAddCompetitorWithSecret(unregisteredCtx, EVENT_NAME, BOAT_CLASS, "", "Mickey Mouse",
                    "USA", registrationLinkSecret, UUID.randomUUID());
            fail("Self-registration should not be possible for a closed regatta!");
        } catch (RuntimeException exc) {
            assertTrue("Response status \"Forbidden\" expected!", exc.getMessage().contains("rc=403"));
        }
    }
}
