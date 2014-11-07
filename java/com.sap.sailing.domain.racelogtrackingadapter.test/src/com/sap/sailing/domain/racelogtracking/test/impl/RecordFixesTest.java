package com.sap.sailing.domain.racelogtracking.test.impl;

import static com.sap.sse.common.Util.size;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.servlet.RecordFixesPostServlet;
import com.sap.sailing.domain.racelogtracking.test.AbstractGPSFixStoreTest;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sse.common.Util;

public class RecordFixesTest extends AbstractGPSFixStoreTest {
    private RecordFixesPostServlet servlet;
    private final DeviceAndSessionIdentifierWithGPSFixesDeserializer deserializer =
            new MockDeviceAndSessioinIdentifierWithGPSFixesDeserializer();
    private final DeviceIdentifier device = new SmartphoneImeiIdentifier("a");

    @Before
    public void setupServlet() {
        servlet = new RecordFixesPostServlet();
        servlet = spy(servlet);
        when(servlet.getRequestDeserializer()).thenReturn(deserializer);
        doReturn(service).when(servlet).getService();
    }

    @Test
    public void areFixesStoredInDb() throws TransformationException, NoCorrespondingServiceRegisteredException {
        map(comp, device, 0, 600);

        int timepoint = 343;
        List<GPSFix> fixes = new ArrayList<>();
        fixes.add(createFix(timepoint, 0, 0, 0, 0));
        fixes.add(createFix(timepoint+1, 0, 0, 0, 0));
        Util.Triple<DeviceIdentifier, Serializable, List<GPSFix>> data = new Util.Triple<>(device, null, fixes);
        servlet.process(null, data);

        DynamicGPSFixMovingTrackImpl<Competitor> track = new DynamicGPSFixMovingTrackImpl<>(comp, 0);
        store.loadCompetitorTrack(track, raceLog, comp);
        track.lockForRead();
        assertEquals(2, size(track.getRawFixes()));
        assertEquals(timepoint, track.getFirstRawFix().getTimePoint().asMillis());
        assertTrue(track.getFirstRawFix() instanceof GPSFixMoving);
        track.unlockAfterRead();
    }
}
