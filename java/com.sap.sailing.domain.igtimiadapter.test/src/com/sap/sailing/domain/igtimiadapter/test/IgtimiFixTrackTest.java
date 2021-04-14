package com.sap.sailing.domain.igtimiadapter.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.GERMAN);
        TimePoint start = new MillisecondsTimePoint(dateFormat.parse("2013-11-09T07:00:00Z"));
        TimePoint end   = new MillisecondsTimePoint(dateFormat.parse("2013-11-09T07:10:00Z"));
        // URL is https://www.igtimi.com/api/v1/devices/data_access_windows?type=read&start_time=1383811200000&end_time=1383933600000&access_token=3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9
        Iterable<DataAccessWindow> daws = connection.getDataAccessWindows(Permission.read, start, end, /* deviceSerialNumbers; get all devices available for that time */ null);
        Set<String> deviceSerialNumbers = new HashSet<>();
        for (DataAccessWindow daw : daws) {
            deviceSerialNumbers.add(daw.getDeviceSerialNumber());
        }
        logger.info("Retrieving resource data as tracks...");
        Map<String, Map<Type, DynamicTrack<Fix>>> data = connection.getResourceDataAsTracks(start, end, deviceSerialNumbers, Type.gps_latlong, Type.AWA, Type.AWS, Type.HDG, Type.HDGM);
        logger.info("Successfully retrieved resource data as tracks");
        assertFalse(data.isEmpty());
        Map<Type, DynamicTrack<Fix>> windSensorMap = data.get("DD-EE-AAHG");
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
