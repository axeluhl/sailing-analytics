package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.MarkApi;
import com.sap.sailing.selenium.api.event.MarkApi.Mark;
import com.sap.sailing.selenium.api.event.RegattaApi;
import com.sap.sailing.selenium.api.event.RegattaApi.Regatta;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class MarkApiTest extends AbstractSeleniumTest {

    private static String EVENT_NAME = "MarkApiTestEvent";
    private static String BOAT_CLASS = "Flying Dutchman";

    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final MarkApi markApi = new MarkApi();

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testAddMarkToRegatta() {
        final ApiContext ctx = createApiContext(getContextRoot(), SERVER_CONTEXT, "admin", "admin");

        eventApi.createEvent(ctx, EVENT_NAME, BOAT_CLASS, CompetitorRegistrationType.CLOSED, "default");
        final Regatta regatta = regattaApi.getRegatta(ctx, EVENT_NAME);
        final Mark mark = markApi.addMarkToRegatta(ctx, regatta.getName(), "Startboat");

        assertNotNull("Mark result should not be null", mark);
        assertNotNull("Id of created mark should not be null", mark.getMarkId());
    }
}
