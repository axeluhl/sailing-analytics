package com.sap.sailing.domain.igtimiadapter.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWA;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWS;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDGM;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.test.AbstractTestWithIgtimiConnection;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * Igtimi fixes come as isolated single fixes from separate sensors, and even if they are attached to the same device,
 * their time stamps may not be synchronized. Therefore, separate {@link Track} tracks will be used to hold the data
 * coming from the different sensors attached to the same device, and several of them need to be joined to allow for the
 * construction, e.g., of a single {@link Wind} of {@link GPSFixMoving} fix.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class IgtimiFixTrackTest extends AbstractTestWithIgtimiConnection {
    private static final Logger logger = Logger.getLogger(IgtimiFixTrackTest.class.getName());
    
    @Test
    public void testFetchFixesIntoTracks() throws Exception {
        final String DEVICE_SERIAL_NUMBER = "DD-EE-AAHG";
        final List<Fix> fixes = new ArrayList<>();
        // Type.gps_latlong, Type.AWA, Type.AWS, Type.HDG, Type.HDGM);
        final SensorImpl sensor = new SensorImpl(DEVICE_SERIAL_NUMBER, 0);
        fixes.add(new AWA(TimePoint.now(), sensor, new DegreeBearingImpl(123.0)));
        fixes.add(new AWS(TimePoint.now(), sensor, new KnotSpeedImpl(12.0)));
        fixes.add(new HDGM(TimePoint.now(), sensor, new DegreeBearingImpl(86.0)));
        final Map<String, Map<Type, DynamicTrack<Fix>>> data = ((IgtimiConnectionImpl) connection).getFixesAsTracks(fixes);
        logger.info("Successfully retrieved resource data as tracks");
        assertFalse(data.isEmpty());
        final Map<Type, DynamicTrack<Fix>> windSensorMap = data.get(DEVICE_SERIAL_NUMBER);
        assertNotNull(windSensorMap);
        assertTrue(windSensorMap.containsKey(Type.AWA));
        assertTrue(windSensorMap.containsKey(Type.AWS));
        assertTrue(windSensorMap.containsKey(Type.HDGM));
        final DynamicTrack<Fix> awaTrack = windSensorMap.get(Type.AWA);
        assertFalse(isEmpty(awaTrack));
        final DynamicTrack<Fix> awsTrack = windSensorMap.get(Type.AWS);
        assertFalse(isEmpty(awsTrack));
        final DynamicTrack<Fix> hdgmTrack = windSensorMap.get(Type.HDGM);
        assertFalse(isEmpty(hdgmTrack));
    }

    private boolean isEmpty(final DynamicTrack<Fix> track) {
        track.lockForRead();
        try {
            return Util.isEmpty(track.getRawFixes());
        } finally {
            track.unlockAfterRead();
        }
    }
}
