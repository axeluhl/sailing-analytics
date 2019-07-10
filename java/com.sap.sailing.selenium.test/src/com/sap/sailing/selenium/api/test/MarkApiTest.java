package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.MarkApi;
import com.sap.sailing.selenium.api.event.MarkApi.Mark;
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
        markApi.addMarkFix(ctx, EVENT_NAME, race.getRaceName(), "Default", mark.getMarkId(), 9.12, .599,
                currentTimeMillis());
    }
}
