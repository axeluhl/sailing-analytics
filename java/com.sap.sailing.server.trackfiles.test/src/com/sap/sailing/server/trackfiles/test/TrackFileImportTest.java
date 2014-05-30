package com.sap.sailing.server.trackfiles.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter.Callback;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.server.trackfiles.impl.RouteConverterGPSFixImporterImpl;

public class TrackFileImportTest {
    private boolean callbackCalled = false;
    
    @Before
    public void setup() {
        callbackCalled = false;
    }
    
    @Test
    public void testGpx() throws IOException, FormatNotSupportedException {
        InputStream in = getClass().getResourceAsStream("/Cardiff Race17 - COMPETITORS.gpx");
        new RouteConverterGPSFixImporterImpl().importFixes(in, new Callback() {
            @Override
            public void addFix(GPSFix fix) {
                if (fix instanceof GPSFixMoving) {
                    callbackCalled = true;
                }
            }

            @Override
            public void startTrack(String name, Map<String, String> properties) {
            }
        }, false);
        assertTrue(callbackCalled);
    }
    
    @Test
    public void testKmlOnlyLatLong() throws IOException, FormatNotSupportedException {
        InputStream in = getClass().getResourceAsStream("/sam002903 - COMPETITORS.kml");
        new RouteConverterGPSFixImporterImpl().importFixes(in, new Callback() {
            @Override
            public void startTrack(String name, Map<String, String> properties) {
            }

            @Override
            public void addFix(GPSFix fix) {
                if (fix instanceof GPSFix) {
                    callbackCalled = true;
                }
            }
        }, false);
        assertTrue(callbackCalled);
    }
}
