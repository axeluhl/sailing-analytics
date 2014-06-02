package com.sap.sailing.server.gateway.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.gateway.trackfiles.impl.TrackFilesImportServlet;
import com.sap.sailing.server.trackfiles.RouteConverterGPSFixImporterFactory;
import com.sap.sse.common.Util;

public class GPSFixImportTest {
    
    @Test
    public void testReusingImportStream() throws IOException {
        TrackFilesImportServlet servlet = spy(new TrackFilesImportServlet());
        doReturn(Arrays.asList((GPSFixImporter) RouteConverterGPSFixImporterFactory.INSTANCE.createRouteConverterGPSFixImporter())).
                when(servlet).getGPSFixImporters(anyString());
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(servlet).storeFix(any(GPSFix.class), any(DeviceIdentifier.class));
        
        InputStream in = getClass().getResourceAsStream("/Cardiff Race17 - COMPETITORS.gpx");
        servlet.importFiles(Arrays.asList(new Util.Pair<>("test.gpx", in)), new AlwaysFailingGPSFixImporter());
        // getting to here without errors is good enough
    }
}
