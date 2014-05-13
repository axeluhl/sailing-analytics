package com.sap.sailing.server.trackfiles.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.server.trackfiles.Import;
import com.sap.sailing.server.trackfiles.Import.FixCallback;

public class TrackFileImportTest {
    private boolean callbackCalled = false;
    
    @Before
    public void setup() {
        callbackCalled = false;
    }
    
    @Test
    public void testGpx() throws IOException {
        InputStream in = getClass().getResourceAsStream("/Cardiff Race17 - COMPETITORS.gpx");
        Import.INSTANCE.importFixes(in, new FixCallback() {
            @Override
            public void addFix(GPSFix fix, int numberOfFixes, TimeRange timeRange, String trackName) {
                if (fix instanceof GPSFixMoving) {
                    callbackCalled = true;
                }
            }
        }, false);
        assertTrue(callbackCalled);
    }
    
    @Test
    public void testKmlOnlyLatLong() throws IOException {
        InputStream in = getClass().getResourceAsStream("/sam002903 - COMPETITORS.kml");
        Import.INSTANCE.importFixes(in, new FixCallback() {
            @Override
            public void addFix(GPSFix fix, int numberOfFixes, TimeRange timeRange, String trackName) {
                if (fix instanceof GPSFix) {
                    callbackCalled = true;
                }
            }
        }, false);
        assertTrue(callbackCalled);
    }
}
