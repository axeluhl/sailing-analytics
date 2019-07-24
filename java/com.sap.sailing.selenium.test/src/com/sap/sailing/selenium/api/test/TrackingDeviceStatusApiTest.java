package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.GpsFixMoving.createFix;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.GpsFixMoving;
import com.sap.sailing.selenium.api.event.GpsFixApi;
import com.sap.sailing.selenium.api.event.TrackingDeviceApi;
import com.sap.sailing.selenium.api.event.TrackingDeviceApi.DeviceStatus;
import com.sap.sailing.selenium.api.event.TrackingDeviceApi.GPSFixResponse;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TrackingDeviceStatusApiTest extends AbstractSeleniumTest {

    private final GpsFixApi gpsFixApi = new GpsFixApi();
    private final TrackingDeviceApi trackingDeviceApi = new TrackingDeviceApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void testDeviceStatusWithLastFix() {
        final UUID deviceUUID = UUID.randomUUID();
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final DeviceStatus statusBeforeFix = trackingDeviceApi.getDeviceStatus(ctx, deviceUUID);
        assertEquals(deviceUUID, statusBeforeFix.getDeviceUUID());
        assertNull(statusBeforeFix.getLastFix());
        
        final long fix1Millis = System.currentTimeMillis() - 100;
        final GpsFixMoving fix1 = createFix(49.121, 8.5987, fix1Millis, 10.0, 180.0);
        gpsFixApi.postGpsFix(ctx, deviceUUID, fix1);
        final DeviceStatus statusWithFix1 = trackingDeviceApi.getDeviceStatus(ctx, deviceUUID);
        final GPSFixResponse lastFix1 = statusWithFix1.getLastFix();
        assertNotNull(lastFix1);
        assertEquals(fix1Millis, lastFix1.getTime());
        
        final long fix2Millis = System.currentTimeMillis();
        final GpsFixMoving fix2 = createFix(49.12, 8.599, fix2Millis, 10.0, 180.0);
        gpsFixApi.postGpsFix(ctx, deviceUUID, fix2);
        final DeviceStatus statusWithFix2 = trackingDeviceApi.getDeviceStatus(ctx, deviceUUID);
        assertEquals(fix2Millis, statusWithFix2.getLastFix().getTime());
    }
}
