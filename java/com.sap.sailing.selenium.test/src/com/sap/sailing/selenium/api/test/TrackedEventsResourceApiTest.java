package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.TrackedEventsApi;
import com.sap.sailing.selenium.api.event.TrackedEventsApi.TrackedEvents;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;

public class TrackedEventsResourceApiTest extends AbstractSeleniumTest {

    private final TrackedEventsApi trackedEventsApi = new TrackedEventsApi();

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testGetTrackedEventsEmpty() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, true);
        Assert.assertNotNull(trackedEvents);
        Assert.assertTrue(Util.isEmpty(trackedEvents.getEvents()));
    }

}
