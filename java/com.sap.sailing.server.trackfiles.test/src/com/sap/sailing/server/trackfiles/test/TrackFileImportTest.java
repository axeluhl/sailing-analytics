package com.sap.sailing.server.trackfiles.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

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
    public void testImport() throws IOException {
        InputStream in = getClass().getResourceAsStream("/Cardiff Race17 - COMPETITORS.gpx");
        Import.INSTANCE.importFixes(in, new FixCallback() {
            @Override
            public void addFix(GPSFix fix, String trackName) {
                if (fix instanceof GPSFixMoving) {
                    callbackCalled = true;
                }
            }
            
            @Override
            public void addFix(GPSFix fix) {
                // expecting track names
            }
        }, false);
        assertTrue(callbackCalled);
    }
}
