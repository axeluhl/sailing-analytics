package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.GpsFixMoving.createFix;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.GpsFixMoving;
import com.sap.sailing.selenium.api.event.GpsFixApi;
import com.sap.sailing.selenium.api.event.GpsFixApi.GpsFixResponse;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class GpsFixApiTest extends AbstractSeleniumTest {

    private final GpsFixApi gpsFixApi = new GpsFixApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void testGpsFixHull() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final GpsFixResponse gpsFixRepsone = gpsFixApi.postGpsFix(ctx, UUID.randomUUID());
        assertTrue("Result should be empty.", gpsFixRepsone.isEmpty());
    }

    @Test
    public void testGpsFix() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final GpsFixMoving gpsFix = createFix(49.12, 8.599, System.currentTimeMillis(), 10.0, 180.0);
        final GpsFixResponse gpsFixResponse = gpsFixApi.postGpsFix(ctx, UUID.randomUUID(), gpsFix);
        assertTrue("Result should be empty.", gpsFixResponse.isEmpty());
    }
}
