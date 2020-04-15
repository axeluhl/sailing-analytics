package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.HttpException;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.Mark;
import com.sap.sailing.selenium.api.event.MarkApi;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.regatta.RaceColumn;
import com.sap.sailing.selenium.api.regatta.Regatta;
import com.sap.sailing.selenium.api.regatta.RegattaApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class MarkApiTest extends AbstractSeleniumTest {

    private static String EVENT_NAME = "MarkApiTestEvent";
    private static String BOAT_CLASS = "Flying Dutchman";

    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final MarkApi markApi = new MarkApi();
    private final SecurityApi securityApi = new SecurityApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void testAddMarkToRegatta() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, EVENT_NAME, BOAT_CLASS, CompetitorRegistrationType.CLOSED, "default");
        final Regatta regatta = regattaApi.getRegatta(ctx, EVENT_NAME);
        final Mark mark = markApi.addMarkToRegatta(ctx, regatta.getName(), "Startboat");

        assertNotNull("Mark result should not be null", mark);
        assertNotNull("Id of created mark should not be null", mark.getMarkId());
    }

    @Test
    public void testAddMarkFix() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, EVENT_NAME, BOAT_CLASS, CompetitorRegistrationType.CLOSED, "default");
        final Regatta regatta = regattaApi.getRegatta(ctx, EVENT_NAME);
        final Mark mark = markApi.addMarkToRegatta(ctx, regatta.getName(), "Startboat");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, EVENT_NAME, null, 1)[0];
        markApi.addMarkFix(ctx, EVENT_NAME, race.getRaceName(), "Default", mark.getMarkId(), /* markTemplateId */ null,
                /* markPropertiesId */ null, 9.12, .599, currentTimeMillis());
    }

    @Test
    public void testAddMarkFixWithMarkTemplateAndMarkProperties() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, EVENT_NAME, BOAT_CLASS, CompetitorRegistrationType.CLOSED, "default");
        final Regatta regatta = regattaApi.getRegatta(ctx, EVENT_NAME);
        final Mark mark = markApi.addMarkToRegatta(ctx, regatta.getName(), "Startboat");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, EVENT_NAME, null, 1)[0];
        markApi.addMarkFix(ctx, EVENT_NAME, race.getRaceName(), "Default", mark.getMarkId(),
                /* markTemplateId */ UUID.randomUUID(), /* markPropertiesId */ UUID.randomUUID(), 9.12, .599,
                currentTimeMillis());
    }

    @Test(expected = HttpException.NotFound.class)
    public void testAddMarkToRegattaForNonExistingEvent() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        markApi.addMarkToRegatta(ctx, "NONEVENT", "Startboat");
    }
    
    @Test(expected = HttpException.Unauthorized.class)
    public void testAddMarkToRegattaWithoutPermission() {
        final ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final ApiContext ownerCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        final ApiContext readerCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "donald", "daisy0815");
        
        eventApi.createEvent(ownerCtx, EVENT_NAME, BOAT_CLASS, CompetitorRegistrationType.CLOSED, "default");
        markApi.addMarkToRegatta(readerCtx, EVENT_NAME, "Startboat");
    }
}
