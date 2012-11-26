package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.impl.DistanceCache;
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
    
    /**
     * A test regarding bug 968. Three subsequent fixes at the same position with increasing time stamps each, then jumping
     * to the next position for three seconds. Let's see what distance and SOG do...
     */
    @Test
    public void testJumpyFixes() {
        DynamicGPSFixTrack<Object, GPSFixMoving> track = new DynamicGPSFixMovingTrackImpl<Object>(new Object(),
                /* millisecondsOverWhichToAverage */ 30000l);
        TimePoint start = MillisecondsTimePoint.now();
        TimePoint now = start;
        final DegreePosition startPos = new DegreePosition(0, 0);
        Position pos = startPos;
        SpeedWithBearing speed = new KnotSpeedWithBearingImpl(40, new DegreeBearingImpl(90));
        int NUMBER_OF_FIXES_AT_SAME_POSITION = 3;
        int NUMBER_OF_REAL_FIXES = 10;
        for (int i = 0; i < NUMBER_OF_REAL_FIXES; i++) {
            addFixesWithSamePositionButProgressingTime(track, now, pos, speed, NUMBER_OF_FIXES_AT_SAME_POSITION);
            track.getDistanceTraveled(start, now); // cause DistanceCache to cache a result
            TimePoint nextNow = now.plus(NUMBER_OF_FIXES_AT_SAME_POSITION * 1000);
            pos = pos.translateGreatCircle(speed.getBearing(), speed.travel(now, nextNow));
            now = nextNow;
        }
        assertEquals(speed.travel(start, now.minus(NUMBER_OF_FIXES_AT_SAME_POSITION * 1000)).getMeters(),
                track.getDistanceTraveled(start, now).getMeters(), 0.00000001);
    }

    private void addFixesWithSamePositionButProgressingTime(DynamicGPSFixTrack<Object, GPSFixMoving> track,
            TimePoint start, Position pos, SpeedWithBearing speed, int numberOfFixes) {
        TimePoint now = start;
        for (int i = 0; i < numberOfFixes; i++) {
            GPSFixMoving fix1 = new GPSFixMovingImpl(pos, now, speed);
            track.addGPSFix(fix1);
            now = now.plus(1000);
        }
    }

    @Test
    public void testMaxSpeedForNonMovingTrackWithUpperTimeLimit() {
        DynamicGPSFixTrack<Object, GPSFix> track = new DynamicGPSFixTrackImpl<Object>(new Object(), /* millisecondsOverWhichToAverage */ 30000l);
        GPSFix fix1 = new GPSFixImpl(new DegreePosition(0, 0), new MillisecondsTimePoint(0));
        track.addGPSFix(fix1);
        GPSFix fix2 = new GPSFixImpl(new DegreePosition(1./60., 0), new MillisecondsTimePoint(3600000)); // 1nm in one hour = 1kt
        track.addGPSFix(fix2);
        GPSFix fix3 = new GPSFixImpl(new DegreePosition(3./60., 0), new MillisecondsTimePoint(7200000)); // 2nm in one hour = 2kts
        track.addGPSFix(fix3);
        GPSFix fix4 = new GPSFixImpl(new DegreePosition(4./60., 0), new MillisecondsTimePoint(10800000)); // 1nm in one hour = 1kt
        track.addGPSFix(fix4);
        assertEquals(1., track.getMaximumSpeedOverGround(new MillisecondsTimePoint(0), new MillisecondsTimePoint(3600000)).
                getB().getKnots(), 0.001);
    }

    @Test
    public void testMaxSpeedForNonMovingTrack() {
        DynamicGPSFixTrack<Object, GPSFix> track = new DynamicGPSFixTrackImpl<Object>(new Object(), /* millisecondsOverWhichToAverage */ 30000l);
        GPSFix fix1 = new GPSFixImpl(new DegreePosition(0, 0), new MillisecondsTimePoint(0));
        track.addGPSFix(fix1);
        GPSFix fix2 = new GPSFixImpl(new DegreePosition(1./60., 0), new MillisecondsTimePoint(3600000)); // 1nm in one hour = 1kt
        track.addGPSFix(fix2);
        GPSFix fix3 = new GPSFixImpl(new DegreePosition(3./60., 0), new MillisecondsTimePoint(7200000)); // 2nm in one hour = 2kts
        track.addGPSFix(fix3);
        GPSFix fix4 = new GPSFixImpl(new DegreePosition(4./60., 0), new MillisecondsTimePoint(10800000)); // 1nm in one hour = 1kt
        track.addGPSFix(fix4);
        assertEquals(2., track.getMaximumSpeedOverGround(new MillisecondsTimePoint(0), new MillisecondsTimePoint(10800000)).
                getB().getKnots(), 0.001);
    }
    
    /**
     * The DistanceCache must not contain any intervals pointing backwards because otherwise an endless recursion will
     * result during the next cache look-up. This test performs a {@link GPSFixTrack#getDistanceTraveled(TimePoint, TimePoint)} with
     * <code>from</code> later than <code>to</code> and ensures that no reversed entry is written to the cache.
     */
    @Test
    public void testDistanceTraveledBackwardsQuery() {
        final Set<Triple<TimePoint, TimePoint, Distance>> cacheEntries = new HashSet<>();
        final DistanceCache distanceCache = new DistanceCache("test-DistanceCache") {
            @Override
            public void cache(TimePoint from, TimePoint to, Distance distance) {
                super.cache(from, to, distance);
                cacheEntries.add(new Triple<>(from, to, distance));
            }
            
        };
        DynamicGPSFixTrack<Object, GPSFix> track = new DynamicGPSFixTrackImpl<Object>(new Object(), /* millisecondsOverWhichToAverage */ 30000l) {
            private static final long serialVersionUID = -7277196393160609503L;
            @Override
            protected DistanceCache getDistanceCache() {
                return distanceCache;
            }
        };
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint earlier = now.minus(10000);
        TimePoint later = now.plus(10000);
        Distance result = track.getDistanceTraveled(now, earlier);
        assertEquals(Distance.NULL, result);
        assertTrue(cacheEntries.isEmpty());
        Distance nextResult = track.getDistanceTraveled(now, later);
        assertEquals(Distance.NULL, nextResult);
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
        track.lockForRead();
        try {
            for (Iterator<GPSFixMoving> i = track.getRawFixes().iterator(); i.hasNext(); first = false) {
                GPSFixMoving fix = i.next();
                long millis = fix.getTimePoint().asMillis();
                if (!first) {
                    TimePoint inBetweenTimePoint = new MillisecondsTimePoint((millis + lastMillis) / 2);
                    Position interpolatedPosition = track.getEstimatedRawPosition(inBetweenTimePoint, false);
                    Distance d1 = lastFix.getPosition().getDistance(interpolatedPosition);
                    Distance d2 = interpolatedPosition.getDistance(fix.getPosition());
                    // the interpolated point should be on the great circle, not open a "triangle"
                    assertEquals(lastFix.getPosition().getDistance(fix.getPosition()).getMeters(),
                            d1.getMeters() + d2.getMeters(), 0.00001);
                }
                lastMillis = millis;
                lastFix = fix;
            }
        } finally {
            track.unlockAfterRead();
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
    public void testDistanceTraveledOnSmoothenedTrackThenAddingOutlier() {
        final Set<TimePoint> invalidationCalls = new HashSet<TimePoint>();
        final DistanceCache distanceCache = new DistanceCache("test-DistanceCache") {
            @Override
            public void invalidateAllAtOrLaterThan(TimePoint timePoint) {
                super.invalidateAllAtOrLaterThan(timePoint);
                invalidationCalls.add(timePoint);
            }
        };
        DynamicGPSFixTrack<Object, GPSFix> track = new DynamicGPSFixTrackImpl<Object>(new Object(), /* millisecondsOverWhichToAverage */ 30000l) {
            private static final long serialVersionUID = -7277196393160609503L;
            @Override
            protected DistanceCache getDistanceCache() {
                return distanceCache;
            }
        };
        final int timeBetweenFixesInMillis = 1000;
        Bearing bearing = new DegreeBearingImpl(123);
        Speed speed = new KnotSpeedImpl(7);
        Position p = new DegreePosition(0, 0);
        final MillisecondsTimePoint now = MillisecondsTimePoint.now();
        TimePoint start = now;
        final int steps = 10;
        TimePoint next = null;
        for (int i=0; i<steps; i++) {
            GPSFix fix = new GPSFixImpl(p, start);
            track.addGPSFix(fix);
            next = start.plus(timeBetweenFixesInMillis);
            p = p.translateGreatCircle(bearing, speed.travel(start, next));
            start = next;
            bearing = new DegreeBearingImpl(bearing.getDegrees() + 1);
        }
        invalidationCalls.clear();
        assertEquals(speed.getMetersPerSecond()*(steps-1), track.getDistanceTraveled(now, start).getMeters(), 0.01);
        final Pair<TimePoint, Pair<TimePoint, Distance>> fullIntervalCacheEntry = distanceCache.getEarliestFromAndDistanceAtOrAfterFrom(now,  start);
        assertNotNull(fullIntervalCacheEntry); // no more entry for "to"-value start in cache
        assertEquals(start, fullIntervalCacheEntry.getA());
        assertEquals(now, fullIntervalCacheEntry.getB().getA());
        TimePoint timePointForOutlier = new MillisecondsTimePoint(now.asMillis() + ((int) steps/2) * timeBetweenFixesInMillis + timeBetweenFixesInMillis/2);
        Position outlierPosition = new DegreePosition(90, 90);
        GPSFix outlier = new GPSFixImpl(outlierPosition, timePointForOutlier);
        track.addGPSFix(outlier);
        assertEquals(1, invalidationCalls.size());
        TimePoint timePointOfLastFixBeforeOutlier = track.getLastFixBefore(timePointForOutlier).getTimePoint();
        assertTrue(invalidationCalls.iterator().next().after(timePointOfLastFixBeforeOutlier)); // outlier doesn't turn its preceding element into an outlier
        assertNull(distanceCache.getEarliestFromAndDistanceAtOrAfterFrom(now,  start)); // no more entry for "to"-value start in cache
        invalidationCalls.clear();
        final TimePoint timePointOfLastOriginalFix = track.getLastRawFix().getTimePoint();
        assertEquals(speed.getMetersPerSecond() * (steps - 1),
                track.getDistanceTraveled(now, timePointOfLastOriginalFix).getMeters(), 0.01);
        final Pair<TimePoint, Pair<TimePoint, Distance>> newFullIntervalCacheEntry = distanceCache
                .getEarliestFromAndDistanceAtOrAfterFrom(now, timePointOfLastOriginalFix);
        assertNotNull(newFullIntervalCacheEntry); // no more entry for "to"-value start in cache
        assertEquals(timePointOfLastOriginalFix, newFullIntervalCacheEntry.getA());
        assertEquals(now, newFullIntervalCacheEntry.getB().getA());
        TimePoint timePointForLateOutlier = new MillisecondsTimePoint(now.asMillis() + (steps-1)*timeBetweenFixesInMillis + timeBetweenFixesInMillis/2);
        Position lateOutlierPosition = new DegreePosition(90, 90);
        GPSFix lateOutlier = new GPSFixImpl(lateOutlierPosition, timePointForLateOutlier);
        track.addGPSFix(lateOutlier);
        assertEquals(1, invalidationCalls.size());
        TimePoint timePointOfLastFixBeforeLateOutlier = track.getLastFixBefore(timePointForLateOutlier).getTimePoint();
        assertTrue(invalidationCalls.iterator().next().after(timePointOfLastFixBeforeLateOutlier));
        invalidationCalls.clear();
        // expect the invalidation to have started after the single cache entry, so the cache entry still has to be there:
        final Pair<TimePoint, Pair<TimePoint, Distance>> stillPresentFullIntervalCacheEntry = distanceCache
                .getEarliestFromAndDistanceAtOrAfterFrom(now, timePointOfLastOriginalFix);
        assertNotNull(stillPresentFullIntervalCacheEntry); // no more entry for "to"-value start in cache
        assertEquals(timePointOfLastOriginalFix, stillPresentFullIntervalCacheEntry.getA());
        assertEquals(now, stillPresentFullIntervalCacheEntry.getB().getA());
        GPSFix polishedLastFix = track.getLastFixBefore(new MillisecondsTimePoint(Long.MAX_VALUE)); // get the last smoothened fix...
        // ...which now still is expected to be the lateOutlier because no succeeding fix qualifies it as outlier:
        assertEquals(lateOutlier, polishedLastFix);
        track.lockForRead();
        try {
            assertEquals(steps+1, Util.size(track.getFixes())); // what will later be detected as outlier is now an additional fix
        } finally {
            track.unlockAfterRead();
        }
        // now add another "normal" fix, making the lateOutlier really an outlier
        GPSFix fix = new GPSFixImpl(p, start); // the "overshoot" from the previous loop can be used to generate the next "regular" fix
        track.addGPSFix(fix);
        assertEquals(1, invalidationCalls.size());
        // now assert that the fix addition also invalidated what is now detected as an outlier
        assertEquals(timePointForLateOutlier, invalidationCalls.iterator().next());
        assertTrue(timePointForLateOutlier.compareTo(fix.getTimePoint()) < 0);
        // expect the invalidation to have started at the outlier, leaving the previous result ending at the fix right before the outlier intact
        final Pair<TimePoint, Pair<TimePoint, Distance>> stillStillPresentFullIntervalCacheEntry = distanceCache
                .getEarliestFromAndDistanceAtOrAfterFrom(now, timePointOfLastOriginalFix);
        assertNotNull(stillStillPresentFullIntervalCacheEntry); // no more entry for "to"-value start in cache
        assertEquals(timePointOfLastOriginalFix, stillStillPresentFullIntervalCacheEntry.getA());
        assertEquals(now, stillStillPresentFullIntervalCacheEntry.getB().getA());
        track.lockForRead();
        try {
            assertEquals(steps+1, Util.size(track.getFixes())); // the one "normal" late fix is added on top of the <steps> fixes, but the two outliers should now be removed
        } finally {
            track.unlockAfterRead();
        }
        GPSFix polishedLastFix2 = track.getLastFixBefore(new MillisecondsTimePoint(Long.MAX_VALUE)); // get the last smoothened fix...
        assertEquals(fix, polishedLastFix2);
        assertEquals(speed.getMetersPerSecond()*steps, track.getDistanceTraveled(now, start).getMeters(), 0.01);
    }
    
    /**
     * A test case for bug 968. If distances are requested for time intervals ending after the last GPS fix, the position
     * is estimated to be at the last fix known. When another fix arrives, the positions for these time points can now be
     * interpolated, leading to different results and hence to a cache inconsistency in case the cache isn't flushed for the
     * interval between the last and the newest fix. This test case adds a few fixes, then asks a distance traveled for an
     * interval ending after the last fix, then adds another fix at a later time point and asks the distance again.
     */
    @Test
    public void testMoreFrequentDistanceComputationThanGPSFixReception() {
        DynamicGPSFixTrack<Object, GPSFix> track = new DynamicGPSFixTrackImpl<Object>(new Object(), /* millisecondsOverWhichToAverage */ 30000l);
        final int timeBetweenFixesInMillis = 1000;
        Bearing bearing = new DegreeBearingImpl(123);
        Speed speed = new KnotSpeedImpl(7);
        Position p = new DegreePosition(0, 0);
        final MillisecondsTimePoint now = MillisecondsTimePoint.now();
        TimePoint start = now;
        final int steps = 10;
        TimePoint next = null;
        GPSFix fix = null;
        for (int i=0; i<steps; i++) {
            fix = new GPSFixImpl(p, start);
            track.addGPSFix(fix);
            next = start.plus(timeBetweenFixesInMillis);
            p = p.translateGreatCircle(bearing, speed.travel(start, next));
            start = next;
            bearing = new DegreeBearingImpl(bearing.getDegrees() + 1);
        }
        Distance distance1 = track.getDistanceTraveled(now, next.minus(timeBetweenFixesInMillis));
        Distance distance2 = track.getDistanceTraveled(now, next.minus(2*timeBetweenFixesInMillis/3));
        Distance distance3 = track.getDistanceTraveled(now, next.minus(timeBetweenFixesInMillis/3));
        Distance distance4 = track.getDistanceTraveled(now, next);
        assertEquals(distance1, distance2); // no progress after time point "next" because no further fixes are known
        assertEquals(distance1, distance3); // no progress after time point "next" because no further fixes are known
        assertEquals(distance1, distance4); // no progress after time point "next" because no further fixes are known
        // now add one more fix
        fix = new GPSFixImpl(p, start);
        track.addGPSFix(fix);
        Distance distance1_new = track.getDistanceTraveled(now, next.minus(timeBetweenFixesInMillis));
        Distance distance2_new = track.getDistanceTraveled(now, next.minus(2*timeBetweenFixesInMillis/3));
        Distance distance3_new = track.getDistanceTraveled(now, next.minus(timeBetweenFixesInMillis/3));
        Distance distance4_new = track.getDistanceTraveled(now, next);
        assertEquals(distance1, distance1_new);
        assertFalse(distance2.equals(distance2_new));
        assertFalse(distance3.equals(distance3_new));
        assertFalse(distance4.equals(distance4_new));
        Distance toReachInOneThirdOfTimeBetweenFixesInMillis = speed.travel(next, next.plus(timeBetweenFixesInMillis/3));
        assertEquals(toReachInOneThirdOfTimeBetweenFixesInMillis.getMeters(), distance2_new.getMeters()-distance1_new.getMeters(), 0.01);
        assertEquals(toReachInOneThirdOfTimeBetweenFixesInMillis.getMeters(), distance3_new.getMeters()-distance2_new.getMeters(), 0.01);
        assertEquals(toReachInOneThirdOfTimeBetweenFixesInMillis.getMeters(), distance4_new.getMeters()-distance3_new.getMeters(), 0.01);
    }
    
    @Test
    public void testDistanceCacheAccessForPartialStrip() {
        final Set<TimePoint> invalidationCalls = new HashSet<TimePoint>();
        final DistanceCache distanceCache = new DistanceCache("test-DistanceCache") {
            @Override
            public void invalidateAllAtOrLaterThan(TimePoint timePoint) {
                super.invalidateAllAtOrLaterThan(timePoint);
                invalidationCalls.add(timePoint);
            }
        };
        DynamicGPSFixTrack<Object, GPSFix> track = new DynamicGPSFixTrackImpl<Object>(new Object(), /* millisecondsOverWhichToAverage */ 30000l) {
            private static final long serialVersionUID = -7277196393160609503L;
            @Override
            protected DistanceCache getDistanceCache() {
                return distanceCache;
            }
        };
        final int timeBetweenFixesInMillis = 1000;
        Bearing bearing = new DegreeBearingImpl(123);
        Speed speed = new KnotSpeedImpl(7);
        Position p = new DegreePosition(0, 0);
        final MillisecondsTimePoint now = MillisecondsTimePoint.now();
        TimePoint start = now;
        final int steps = 10;
        TimePoint next = null;
        for (int i=0; i<steps; i++) {
            GPSFix fix = new GPSFixImpl(p, start);
            track.addGPSFix(fix);
            next = start.plus(timeBetweenFixesInMillis);
            p = p.translateGreatCircle(bearing, speed.travel(start, next));
            start = next;
            bearing = new DegreeBearingImpl(bearing.getDegrees() + 1);
        }
        invalidationCalls.clear();
        final TimePoint stripFrom = now.plus(timeBetweenFixesInMillis);
        final TimePoint stripTo = start.minus(2*timeBetweenFixesInMillis);
        assertEquals(speed.getMetersPerSecond()*(steps-3),
                track.getDistanceTraveled(stripFrom, stripTo).getMeters(), 0.01);
        assertEquals(speed.getMetersPerSecond()*(steps-1), track.getDistanceTraveled(now, start).getMeters(), 0.01);
        assertTrue(invalidationCalls.isEmpty());
        // expect a cache entry exactly for the strip's boundaries
        Pair<TimePoint, Pair<TimePoint, Distance>> stripCacheEntry = distanceCache.getEarliestFromAndDistanceAtOrAfterFrom(stripFrom, stripTo);
        assertEquals(stripTo, stripCacheEntry.getA());
        assertEquals(stripFrom, stripCacheEntry.getB().getA());
        // expect a cache entry exactly for the full boundaries
        Pair<TimePoint, Pair<TimePoint, Distance>> fullCacheEntry = distanceCache.getEarliestFromAndDistanceAtOrAfterFrom(now, start);
        assertEquals(start, fullCacheEntry.getA());
        assertEquals(now, fullCacheEntry.getB().getA());
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
        TimePoint normalFixesTime = null;
        track.lockForRead();
        try {
            Iterator<GPSFixMoving> iter = track.getRawFixes().iterator();
            for (int i = 0; i < 2; i++) {
                normalFixesTime = iter.next().getTimePoint();
            }
            assertNotNull(normalFixesTime);
        } finally {
            track.unlockAfterRead();
        }
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
    
    @Test
    public void testInvalidationIntervalBeginningForPositionEstimation() {
        track.lockForRead();
        final GPSFixMoving firstFixSoFar;
        try {
            firstFixSoFar = track.getFixes().iterator().next();
        } finally {
            track.unlockAfterRead();
        }
        assertNotNull(firstFixSoFar);
        TimePoint beginningOfTime = new MillisecondsTimePoint(0);
        Position positionAtBeginningOfTime = track.getEstimatedPosition(beginningOfTime, /* extrapolate */false);
        long timespan = 2 /* hours */ * 3600 /* seconds/hour */ * 1000 /* millis/s */;
        SpeedWithBearing speed = new KnotSpeedWithBearingImpl(45, new DegreeBearingImpl(123));
        TimePoint slightlyBeforeFirstFix = firstFixSoFar.getTimePoint().minus(timespan);
        Position newPosition = firstFixSoFar.getPosition().translateGreatCircle(speed.getBearing().reverse(),
                speed.travel(firstFixSoFar.getTimePoint(), firstFixSoFar.getTimePoint().plus(timespan)));
        GPSFixMoving newFirstFix = new GPSFixMovingImpl(newPosition, slightlyBeforeFirstFix, speed);
        track.addGPSFix(newFirstFix);
        Pair<TimePoint, TimePoint> intervalAffected = track.getEstimatedPositionTimePeriodAffectedBy(newFirstFix);
        Position newPositionAtBeginningOfTime = track.getEstimatedPosition(beginningOfTime, /* extrapolate */false);
        assertFalse(newPositionAtBeginningOfTime.equals(positionAtBeginningOfTime));
        assertTrue(!intervalAffected.getA().after(beginningOfTime));
    }

    @Test
    public void testInvalidationIntervalEndForPositionEstimation() {
        TimePoint endOfTime = new MillisecondsTimePoint(Long.MAX_VALUE);
        final GPSFixMoving lastFixSoFar = track.getLastFixBefore(endOfTime);
        assertNotNull(lastFixSoFar);
        Position positionAtEndOfTime = track.getEstimatedPosition(endOfTime, /* extrapolate */false);
        long timespan = 2 /* hours */ * 3600 /* seconds/hour */ * 1000 /* millis/s */;
        SpeedWithBearing speed = new KnotSpeedWithBearingImpl(45, new DegreeBearingImpl(123));
        TimePoint slightlyAfterLastFix = lastFixSoFar.getTimePoint().plus(timespan);
        Position newPosition = lastFixSoFar.getPosition().translateGreatCircle(speed.getBearing(),
                speed.travel(lastFixSoFar.getTimePoint(), lastFixSoFar.getTimePoint().plus(timespan)));
        GPSFixMoving newLastFix = new GPSFixMovingImpl(newPosition, slightlyAfterLastFix, speed);
        track.addGPSFix(newLastFix);
        Pair<TimePoint, TimePoint> intervalAffected = track.getEstimatedPositionTimePeriodAffectedBy(newLastFix);
        Position newPositionAtEndOfTime = track.getEstimatedPosition(endOfTime, /* extrapolate */false);
        assertFalse(newPositionAtEndOfTime.equals(positionAtEndOfTime));
        assertTrue(!intervalAffected.getB().before(endOfTime));
    }

    @Test
    public void testInvalidationIntervalForPositionEstimationForEmptyTrack() {
        TimePoint now = MillisecondsTimePoint.now();
        DynamicGPSFixMovingTrackImpl<Object> myTrack = new DynamicGPSFixMovingTrackImpl<Object>(new Object(), /* millisecondsOverWhichToAverage */5000);
        TimePoint beginningOfTime = new MillisecondsTimePoint(0);
        Position positionAtBeginningOfTime = myTrack.getEstimatedPosition(beginningOfTime, /* extrapolate */false);
        TimePoint endOfTime = new MillisecondsTimePoint(Long.MAX_VALUE);
        Position positionAtEndOfTime = myTrack.getEstimatedPosition(endOfTime, /* extrapolate */false);
        assertNull(positionAtBeginningOfTime);
        assertNull(positionAtEndOfTime);
        GPSFixMoving newFix = new GPSFixMovingImpl(new DegreePosition(12, 34), now, new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(123)));
        myTrack.addGPSFix(newFix);
        Pair<TimePoint, TimePoint> intervalAffected = myTrack.getEstimatedPositionTimePeriodAffectedBy(newFix);
        Position newPositionAtBeginningOfTime = myTrack.getEstimatedPosition(beginningOfTime, /* extrapolate */false);
        Position newPositionAtEndOfTime = myTrack.getEstimatedPosition(now, /* extrapolate */false);
        assertFalse(newPositionAtBeginningOfTime.equals(positionAtBeginningOfTime));
        assertTrue(!intervalAffected.getA().after(beginningOfTime));
        assertFalse(newPositionAtEndOfTime.equals(positionAtEndOfTime));
        assertTrue(!intervalAffected.getB().before(endOfTime));
    }
}
