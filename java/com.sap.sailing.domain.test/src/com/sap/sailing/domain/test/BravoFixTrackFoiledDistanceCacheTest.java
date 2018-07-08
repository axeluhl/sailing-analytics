package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.common.sensordata.BravoExtendedSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.BravoExtendedFixImpl;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicBravoFixTrack;
import com.sap.sailing.domain.tracking.impl.BravoFixTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * See bug4629. This test reproduces an order of fix insertion into a {@link BravoFixTrack}, cache invalidation,
 * cache value calculation and cache value insertion that with bug 4629 existing will lead to an inconsistent
 * cache entry that should have been invalidated by the fix insertion.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class BravoFixTrackFoiledDistanceCacheTest {
    private DynamicBravoFixTrack<CourseArea> track;
    private DynamicGPSFixMovingTrackImpl<CourseArea> gpsTrack;
    
    @Before
    public void setUp() {
        final CourseAreaImpl courseArea = new CourseAreaImpl("Test", UUID.randomUUID());
        gpsTrack = new DynamicGPSFixMovingTrackImpl<>(courseArea, /* millisecondsOverWhichToAverage */ 15000);
        track = new BravoFixTrackImpl<>(courseArea, "test", /* hasExtendedFixes */ true, gpsTrack);
        track.add(createFix(1000l, /* rideHeightPort */ 0.6, /* rideHeightStarboard */ 0.6, /* heel */ 10., /* pitch */ 5.));
        track.add(createFix(2000l, /* rideHeightPort */ 0.6, /* rideHeightStarboard */ 0.6, /* heel */ 10., /* pitch */ 5.));
        track.add(createFix(3000l, /* rideHeightPort */ 0.6, /* rideHeightStarboard */ 0.6, /* heel */ 10., /* pitch */ 5.));
        gpsTrack.add(createGPSFix(1000l, 0, 0, 0, 1));
        gpsTrack.add(createGPSFix(2000l, 1./3600./60., 0, 0, 1));
        gpsTrack.add(createGPSFix(3000l, 2./3600./60., 0, 0, 1));
    }
    
    @Test
    public void testDistanceSpentFoiling() {
        assertEquals(new NauticalMileDistance(2./3600.).getMeters(), track.getDistanceSpentFoiling(t(1000l), t(3000l)).getMeters(), 0.01);
    }

    private BravoExtendedFixImpl createFix(long timePointAsMillis, Double rideHeightPort, Double rideHeightStarboard, Double heel, Double pitch) {
        final Double[] fixData = new Double[Collections.max(Arrays.asList(
                BravoExtendedSensorDataMetadata.HEEL.getColumnIndex()+1,
                BravoExtendedSensorDataMetadata.PITCH.getColumnIndex()+1,
                BravoExtendedSensorDataMetadata.RIDE_HEIGHT_PORT_HULL.getColumnIndex()+1,
                BravoExtendedSensorDataMetadata.RIDE_HEIGHT_STBD_HULL.getColumnIndex()+1))];
        fixData[BravoExtendedSensorDataMetadata.HEEL.getColumnIndex()] = heel;
        fixData[BravoExtendedSensorDataMetadata.PITCH.getColumnIndex()] = pitch;
        fixData[BravoExtendedSensorDataMetadata.RIDE_HEIGHT_PORT_HULL.getColumnIndex()] = rideHeightPort;
        fixData[BravoExtendedSensorDataMetadata.RIDE_HEIGHT_STBD_HULL.getColumnIndex()] = rideHeightStarboard;
        return new BravoExtendedFixImpl(new DoubleVectorFixImpl(t(timePointAsMillis), fixData));
    }
    
    private GPSFixMoving createGPSFix(long timePointAsMillis, double lat, double lng, double cogInDeg, double sogInKnots) {
        return new GPSFixMovingImpl(new DegreePosition(lat, lng), new MillisecondsTimePoint(timePointAsMillis),
                new KnotSpeedWithBearingImpl(sogInKnots, new DegreeBearingImpl(cogInDeg)));
    }

    private MillisecondsTimePoint t(long timePointAsMillis) {
        return new MillisecondsTimePoint(timePointAsMillis);
    }
}
