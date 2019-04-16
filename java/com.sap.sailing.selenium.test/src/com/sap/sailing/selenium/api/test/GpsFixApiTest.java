package com.sap.sailing.selenium.api.test;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.GPSFixApi;
import com.sap.sailing.selenium.api.event.GPSFixApi.GpsFixResponse;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class GpsFixApiTest extends AbstractSeleniumTest {

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testGpsFixHull() {
        ApiContext ctx = ApiContext.createApiContext(getContextRoot(), ApiContext.SERVER_CONTEXT, "admin", "admin");
        GPSFixApi gpsFixesApi = new GPSFixApi();
        GpsFixResponse gpsFixRepsone = gpsFixesApi.postGpsFix(ctx, UUID.randomUUID());
        assertTrue("Result should be empty.", gpsFixRepsone.getJson().isEmpty());
    }

    @Test
    public void testGpsFix() {
        ApiContext ctx = ApiContext.createApiContext(getContextRoot(), ApiContext.SERVER_CONTEXT, "admin", "admin");
        GPSFixApi gpsFixesApi = new GPSFixApi();
        GpsFixResponse gpsFixResponse = gpsFixesApi.postGpsFix(ctx, UUID.randomUUID(),
                gpsFixesApi.new GpsFix(49.12, 8.599, System.currentTimeMillis(), 10.0, 180.0));
        System.out.println(gpsFixResponse.getJson().toJSONString());
        assertTrue("Result should be empty.", gpsFixResponse.getJson().isEmpty());
    }
}
