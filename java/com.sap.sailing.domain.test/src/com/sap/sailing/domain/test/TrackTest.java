package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.TrackImpl;

public class TrackTest {
    private DynamicTrackImpl<Boat, GPSFixMoving> track;
    private GPSFixMovingImpl gpsFix1;
    private GPSFixMovingImpl gpsFix2;
    private GPSFixMovingImpl gpsFix3;
    private GPSFixMovingImpl gpsFix4;
    private GPSFixMovingImpl gpsFix5;

    @Before
    public void setUp() throws InterruptedException {
        track = new DynamicGPSFixMovingTrackImpl<Boat>(new BoatImpl("MyFirstBoat", new BoatClassImpl("505", /* typicallyStartsUpwind */
        true), null), /* millisecondsOverWhichToAverage */5000, /* no smoothening */null);
        TimePoint now1 = MillisecondsTimePoint.now();
        TimePoint now2 = addMillisToTimepoint(now1, 3);
        DegreePosition position1 = new DegreePosition(1, 2);
        DegreePosition position2 = new DegreePosition(1, 3);
        gpsFix1 = new GPSFixMovingImpl(
                position1, now1, new KnotSpeedWithBearingImpl(position1.getDistance(position2)
                        .inTime(now2.asMillis() - now1.asMillis()).getKnots(),
                        new DegreeBearingImpl(90)));
        gpsFix2 = new GPSFixMovingImpl(position2, now2, new KnotSpeedWithBearingImpl(position1.getDistance(position2)
                .inTime(now2.asMillis() - gpsFix1.getTimePoint().asMillis()).getKnots(), new DegreeBearingImpl(90)));
        TimePoint now3 = addMillisToTimepoint(now2, 3);
        Position position3 = new DegreePosition(1, 4);
        gpsFix3 = new GPSFixMovingImpl(
                position3, now3, new KnotSpeedWithBearingImpl(position2.getDistance(position3)
                        .inTime(now3.asMillis() - gpsFix2.getTimePoint().asMillis()).getKnots(),
                        new DegreeBearingImpl(0)));
        TimePoint now4 = addMillisToTimepoint(now3, 3);
        Position position4 = new DegreePosition(3, 4);
        gpsFix4 = new GPSFixMovingImpl(
                position4, now4, new KnotSpeedWithBearingImpl(position3.getDistance(position4)
                        .inTime(now4.asMillis() - gpsFix3.getTimePoint().asMillis()).getKnots(),
                        new DegreeBearingImpl(0)));
        TimePoint now5 = addMillisToTimepoint(now4, 3);
        Position position5 = new DegreePosition(5, 4);
        gpsFix5 = new GPSFixMovingImpl(position5, now5, new KnotSpeedWithBearingImpl(position4.getDistance(position5)
                .inTime(now5.asMillis() - gpsFix4.getTimePoint().asMillis()).getKnots(), new DegreeBearingImpl(0)));
        track.addGPSFix(gpsFix1);
        track.addGPSFix(gpsFix2);
        track.addGPSFix(gpsFix3);
        track.addGPSFix(gpsFix4);
        track.addGPSFix(gpsFix5);
    }
    
    @Test
    public void testBearingAveragingAcrossZeroDegrees() {
        DynamicGPSFixTrack<Object, GPSFix> track = new DynamicGPSFixTrackImpl<Object>(null, /* millisecondsOverWhichToAverage */ 5000);
        TimePoint t1 = new MillisecondsTimePoint(1000);
        TimePoint t2 = new MillisecondsTimePoint(2000);
        TimePoint t3 = new MillisecondsTimePoint(3000);
        GPSFix f1 = new GPSFixImpl(new DegreePosition(0, 0), t1);
        GPSFix f2 = new GPSFixImpl(new DegreePosition(0.00001, 0.00001), t2);
        GPSFix f3 = new GPSFixImpl(new DegreePosition(0.00002, 0), t3);
        track.addGPSFix(f1);
        track.addGPSFix(f2);
        track.addGPSFix(f3);
        SpeedWithBearing average = track.getEstimatedSpeed(t2);
        assertEquals(0, average.getBearing().getDegrees(), 0.00001);
    }
    
    @Test
    public void testBearingAveragingAcrossZeroDegreesWithGPSFixMoving() {
        DynamicGPSFixMovingTrackImpl<Object> track = new DynamicGPSFixMovingTrackImpl<Object>(null, /* millisecondsOverWhichToAverage */ 5000);
        TimePoint t1 = new MillisecondsTimePoint(1000);
        TimePoint t2 = new MillisecondsTimePoint(2000);
        TimePoint t3 = new MillisecondsTimePoint(3000);
        GPSFixMoving f1 = new GPSFixMovingImpl(new DegreePosition(0, 0), t1, new KnotSpeedWithBearingImpl(1, new DegreeBearingImpl(45)));
        GPSFixMoving f2 = new GPSFixMovingImpl(new DegreePosition(0.00001, 0.00001), t2, new KnotSpeedWithBearingImpl(1, new DegreeBearingImpl(0)));
        GPSFixMoving f3 = new GPSFixMovingImpl(new DegreePosition(0.00002, 0), t3, new KnotSpeedWithBearingImpl(1, new DegreeBearingImpl(315)));
        track.addGPSFix(f1);
        track.addGPSFix(f2);
        track.addGPSFix(f3);
        SpeedWithBearing average = track.getRawEstimatedSpeed(t2);
        assertEquals(0, average.getBearing().getDegrees(), 0.00001);
    }
    
    /**
     * Bug #70: modify a track while iterating over a subset of it; ensure that this causes a
     * {@link ConcurrentModificationException}.
     */
    @Test
    public void testAddingWhileIteratingOverSubset() throws InterruptedException, IllegalArgumentException,
            IllegalAccessException, SecurityException, NoSuchFieldException {
        Field fixesField = TrackImpl.class.getDeclaredField("fixes");
        fixesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        NavigableSet<GPSFixMoving> fixes = (NavigableSet<GPSFixMoving>) fixesField.get(track);
        SortedSet<GPSFixMoving> subset = fixes.subSet(gpsFix2, gpsFix5);
        assertEquals(3, subset.size());
        Iterator<GPSFixMoving> subsetIter = subset.iterator();
        // start iteration
        GPSFixMoving firstOfSubset = subsetIter.next();
        assertEquals(gpsFix2, firstOfSubset);
        // now add a fix:
        TimePoint now6 = addMillisToTimepoint(gpsFix5.getTimePoint(), 3);
        track.addGPSFix(new GPSFixMovingImpl(
                new DegreePosition(6, 5), now6, new KnotSpeedWithBearingImpl(2, new DegreeBearingImpl(0))));
        try {
            GPSFixMoving secondOfSubset = subsetIter.next();
            assertEquals(gpsFix3, secondOfSubset);
            fail("adding a fix interferes with iteration over subSet of track's fixes");
        } catch (ConcurrentModificationException e) {
            // this is what we expected
        }
    }
    
   private TimePoint addMillisToTimepoint(TimePoint p, long millis) {
       return new MillisecondsTimePoint(p.asMillis() + millis);
   }

    @Test
    public void testIterate() {
        track.lockForRead();
        try {
            Iterator<GPSFixMoving> i = track.getRawFixes().iterator();
            int count;
            for (count = 0; i.hasNext(); count++) {
                i.next();
            }
            assertEquals(5, count);
        } finally {
            track.unlockAfterRead();
        }
    }
    
    @Test
    public void testOrdering() {
        long lastMillis = 0;
        GPSFix lastFix = null;
        boolean first = true;
        track.lockForRead();
        try {
            for (Iterator<GPSFixMoving> i = track.getRawFixes().iterator(); i.hasNext(); first = false) {
                GPSFixMoving fix = i.next();
                long millis = fix.getTimePoint().asMillis();
                if (!first) {
                    assertTrue(millis > lastMillis);
                    TimePoint inBetweenTimePoint = new MillisecondsTimePoint((millis + lastMillis) / 2);
                    assertEquals(lastFix, track.getLastRawFixBefore(inBetweenTimePoint));
                    assertEquals(lastFix, track.getLastRawFixAtOrBefore(inBetweenTimePoint));
                    assertEquals(fix, track.getFirstRawFixAfter(inBetweenTimePoint));
                    assertEquals(fix, track.getFirstRawFixAtOrAfter(inBetweenTimePoint));

                    assertEquals(lastFix, track.getLastRawFixAtOrBefore(lastFix.getTimePoint()));
                    assertEquals(fix, track.getFirstRawFixAtOrAfter(fix.getTimePoint()));

                    assertEquals(lastFix, track.getLastRawFixBefore(fix.getTimePoint()));
                    assertEquals(fix, track.getLastRawFixAtOrBefore(fix.getTimePoint()));
                    assertEquals(fix, track.getFirstRawFixAfter(lastFix.getTimePoint()));
                    assertEquals(lastFix, track.getFirstRawFixAtOrAfter(lastFix.getTimePoint()));
                }
                lastMillis = millis;
                lastFix = fix;
            }
        } finally {
            track.unlockAfterRead();
        }
    }
    
    @Test
    public void assertEstimatedPositionBeforeStartIsStart() {
        track.lockForRead();
        try {
            GPSFixMoving start = track.getRawFixes().iterator().next();
            TimePoint oneNanoBeforeStart = new MillisecondsTimePoint(start.getTimePoint().asMillis() - 1);
            assertEquals(start.getPosition(), track.getEstimatedPosition(oneNanoBeforeStart, false));
        } finally {
            track.unlockAfterRead();
        }
    }
    
    @Test
    public void testSimpleInterpolation() {
        long lastMillis = 0;
        GPSFix lastFix = null;
        boolean first = true;
        for (Iterator<GPSFixMoving> i = track.getRawFixes().iterator(); i.hasNext(); first = false) {
            GPSFixMoving fix = i.next();
            long millis = fix.getTimePoint().asMillis();
            if (!first) {
                TimePoint inBetweenTimePoint = new MillisecondsTimePoint((millis+lastMillis)/2);
                Position interpolatedPosition = track.getEstimatedRawPosition(inBetweenTimePoint, false);
                Distance d1 = lastFix.getPosition().getDistance(interpolatedPosition);
                Distance d2 = interpolatedPosition.getDistance(fix.getPosition());
                // the interpolated point should be on the great circle, not open a "triangle"
                assertEquals(
                        lastFix.getPosition().getDistance(fix.getPosition())
                                .getMeters(), d1.getMeters() + d2.getMeters(),
                        0.00001);
            }
            lastMillis = millis;
            lastFix = fix;
        }
    }
    
    @Test
    public void testSimpleExtrapolation() {
        GPSFix fix = track.getLastRawFix();
        long millis = fix.getTimePoint().asMillis();
        GPSFix lastFix = track.getLastRawFixBefore(fix.getTimePoint());
        long lastMillis = lastFix.getTimePoint().asMillis();
        TimePoint afterTimePoint = new MillisecondsTimePoint(millis + (millis-lastMillis));
        Position extrapolatedPosition = track.getEstimatedPosition(afterTimePoint, true);
        assertEquals(0.5, fix.getPosition().getDistance(extrapolatedPosition).getMeters()
                / lastFix.getPosition().getDistance(extrapolatedPosition).getMeters(), 0.00001);
    }
    
    @Test
    public void testExtrapolationDoesntHappenIfSuppressed() {
        GPSFix fix = track.getLastRawFix();
        long millis = fix.getTimePoint().asMillis();
        GPSFix lastFix = track.getLastRawFixBefore(fix.getTimePoint());
        long lastMillis = lastFix.getTimePoint().asMillis();
        TimePoint afterTimePoint = new MillisecondsTimePoint(millis + (millis-lastMillis));
        Position extrapolatedPosition = track.getEstimatedPosition(afterTimePoint, false);
        assertEquals(extrapolatedPosition, fix.getPosition());
    }
    
    @Test
    public void testDistanceTraveled() {
        List<Distance> distances = new ArrayList<Distance>();
        List<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
        boolean first = true;
        GPSFixMoving oldFix = null;
        track.lockForRead();
        try {
            for (GPSFixMoving fix : track.getRawFixes()) {
                fixes.add(fix);
                if (first) {
                    first = false;
                } else {
                    Distance d = oldFix.getPosition().getDistance(fix.getPosition());
                    distances.add(d);
                }
                oldFix = fix;
            }
            for (int i = 0; i < fixes.size(); i++) {
                for (int j = i; j < fixes.size(); j++) {
                    double distanceSumInNauticalMiles = 0;
                    for (int k = i; k < j; k++) {
                        distanceSumInNauticalMiles += distances.get(k).getNauticalMiles();
                    }
                    // travel fully from fix #i to fix #j and require the segment distances to sum up equal
                    double nauticalMilesFromIToJ = track.getRawDistanceTraveled(fixes.get(i).getTimePoint(),
                            fixes.get(j).getTimePoint()).getNauticalMiles();
                    assertEquals(distanceSumInNauticalMiles, nauticalMilesFromIToJ, 0.0000001);
                    if (j > i) {
                        // now skip half a segment at the beginning:
                        double nauticalMilesFromHalfAfterIToJ = track.getRawDistanceTraveled(
                                new MillisecondsTimePoint((fixes.get(i).getTimePoint().asMillis() + fixes.get(i + 1)
                                        .getTimePoint().asMillis()) / 2), fixes.get(j).getTimePoint())
                                .getNauticalMiles();
                        assertTrue("for i=" + i + ", j=" + j + ": " + nauticalMilesFromHalfAfterIToJ + "<"
                                + distanceSumInNauticalMiles,
                                nauticalMilesFromHalfAfterIToJ < distanceSumInNauticalMiles);
                        if (i > 0) {
                            // now skip half a segment before the beginning:
                            double nauticalMilesFromHalfBeforeIToJ = track.getRawDistanceTraveled(
                                    new MillisecondsTimePoint((fixes.get(i).getTimePoint().asMillis() + fixes
                                            .get(i - 1).getTimePoint().asMillis()) / 2), fixes.get(j).getTimePoint())
                                    .getNauticalMiles();
                            assertTrue(nauticalMilesFromHalfBeforeIToJ > distanceSumInNauticalMiles);
                        }
                        // now skip half a segment at the end:
                        double nauticalMilesFromIToHalfBeforeJ = track.getRawDistanceTraveled(
                                fixes.get(i).getTimePoint(),
                                new MillisecondsTimePoint((fixes.get(j).getTimePoint().asMillis() + fixes.get(j - 1)
                                        .getTimePoint().asMillis()) / 2)).getNauticalMiles();
                        assertTrue(nauticalMilesFromIToHalfBeforeJ < distanceSumInNauticalMiles);
                        if (j < fixes.size() - 1) {
                            // now skip half a segment before the beginning:
                            double nauticalMilesFromIToHalfAfterJ = track.getRawDistanceTraveled(
                                    fixes.get(i).getTimePoint(),
                                    new MillisecondsTimePoint((fixes.get(j).getTimePoint().asMillis() + fixes
                                            .get(j + 1).getTimePoint().asMillis()) / 2)).getNauticalMiles();
                            assertTrue("for i=" + i + ", j=" + j + ": " + nauticalMilesFromIToHalfAfterJ + ">"
                                    + distanceSumInNauticalMiles,
                                    nauticalMilesFromIToHalfAfterJ > distanceSumInNauticalMiles);
                        }
                    }
                }
            }
        } finally {
            track.unlockAfterRead();
        }
    }
    
    @Test
    public void testDistanceTraveledOnInBetweenSectionFromFixToFix() {
        track.lockForRead();
        try {
            // take second and third fix and compute distance between them
            Iterator<GPSFixMoving> iter = track.getRawFixes().iterator();
            iter.next(); // skip first;
            GPSFix second = iter.next();
            GPSFix third = iter.next();
            assertEquals(second.getPosition().getDistance(third.getPosition()),
                    track.getRawDistanceTraveled(second.getTimePoint(), third.getTimePoint()));
        } finally {
            track.unlockAfterRead();
        }
    }
    
    @Test
    public void testFarFutureFixNotUsedDuringEstimation() {
        Iterator<GPSFixMoving> iter = track.getRawFixes().iterator();
        TimePoint normalFixesTime = null;
        for (int i=0; i<2; i++) {
            normalFixesTime = iter.next().getTimePoint();
        }
        assertNotNull(normalFixesTime);
        SpeedWithBearing estimatedSpeed = track.getEstimatedSpeed(normalFixesTime);
        GPSFixMovingImpl gpsFixFarInTheFuture = new GPSFixMovingImpl(
                new DegreePosition(89, 180), new MillisecondsTimePoint(
                        System.currentTimeMillis()+10000000l), new KnotSpeedWithBearingImpl(200000, new DegreeBearingImpl(0)));
        track.addGPSFix(gpsFixFarInTheFuture);
        Position estimatedPosNew = track.getEstimatedPosition(normalFixesTime, /* extrapolate */ false);
        // expecting to get the coordinates of gpsFix2's position
        assertEquals(gpsFix2.getPosition().getLatDeg(), estimatedPosNew.getLatDeg(), 0.5);
        assertEquals(gpsFix2.getPosition().getLngDeg(), estimatedPosNew.getLngDeg(), 0.5);
        SpeedWithBearing estimatedSpeedNew = track.getEstimatedSpeed(normalFixesTime);
        assertEquals(estimatedSpeed.getKnots(), estimatedSpeedNew.getKnots(), 0.001);
        assertEquals(estimatedSpeed.getBearing().getDegrees(), estimatedSpeedNew.getBearing().getDegrees(), 0.001);
    }
}
