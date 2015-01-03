package com.sap.sailing.server.gateway.trackfiles.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.trackfiles.RouteConverterGPSFixImporterFactory;
import com.sap.sse.common.Util.Pair;

public class GPSFixImportTest {
    private static class BufferedInputStreamWithPublicBuffer extends BufferedInputStream {
        public BufferedInputStreamWithPublicBuffer(InputStream in) {
            super(in);
        }

        public byte[] getBuffer() {
            return buf;
        }
    }
    
    @Test
    public void testReReadingEOFFromBufferedInputStream() throws IOException {
        InputStream in = getClass().getResourceAsStream("/Cardiff Race17 - COMPETITORS.gpx");
        BufferedInputStreamWithPublicBuffer bis = new BufferedInputStreamWithPublicBuffer(in);
        bis.mark(10*1024*1024); // the file has ~2MB, so 10MB is much more than twice the size, so should never be required
        int size = 0;
        while (bis.read() != -1) {
            size++;
        }
        bis.reset();
        int secondSize = 0;
        while (bis.read() != -1) {
            secondSize++;
        }
        assertEquals(size, secondSize);
        assertTrue(bis.getBuffer().length < 8*1024*1024);
        bis.reset();
        int thirdSize = 0;
        while (bis.read() != -1) {
            thirdSize++;
        }
        assertEquals(size, thirdSize);
        assertTrue(bis.getBuffer().length < 8*1024*1024);
        // Now try to read EOF multiple times
        for (int i=1; i<1000; i++) {
            assertEquals(-1, bis.read());
        }
        bis.close();
    }
    
    @Test
    public void testReusingImportStream() throws IOException {
        TrackFilesImportServlet servlet = new TrackFilesImportServlet() {
            private static final long serialVersionUID = -7636477441858728847L;

            @Override
            public Collection<GPSFixImporter> getGPSFixImporters(String type) {
                return Arrays.asList((GPSFixImporter) RouteConverterGPSFixImporterFactory.INSTANCE
                        .createRouteConverterGPSFixImporter());
            }

            @Override
            public void storeFix(GPSFix fix, DeviceIdentifier deviceIdentifier) {
            }
        };
        InputStream in = getClass().getResourceAsStream("/Cardiff Race17 - COMPETITORS.gpx");
        servlet.importFiles(Arrays.asList(new Pair<>("test.gpx", in)), new AlwaysFailingGPSFixImporter(-1));
        // getting to here without errors is good enough
    }
}
