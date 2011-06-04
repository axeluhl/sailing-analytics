package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class TrackTest {
    private DynamicTrackImpl<Boat, GPSFixMoving> track;

    @Before
    public void setUp() throws InterruptedException {
        track = new DynamicGPSFixMovingTrackImpl<Boat>(new BoatImpl("MyFirstBoat",
                new BoatClassImpl("505")), /* millisecondsOverWhichToAverage */ 5000);
        GPSFixMovingImpl gpsFix1 = new GPSFixMovingImpl(
                new DegreePosition(1, 2), new MillisecondsTimePoint(
                        System.currentTimeMillis()), new KnotSpeedWithBearingImpl(1,
                        new DegreeBearingImpl(90)));
        waitThreeMillis();
        GPSFixMovingImpl gpsFix2 = new GPSFixMovingImpl(
                new DegreePosition(1, 3), new MillisecondsTimePoint(
                        System.currentTimeMillis()), new KnotSpeedWithBearingImpl(1,
                        new DegreeBearingImpl(90)));
        waitThreeMillis();
        GPSFixMovingImpl gpsFix3 = new GPSFixMovingImpl(
                new DegreePosition(1, 4), new MillisecondsTimePoint(
                        System.currentTimeMillis()), new KnotSpeedWithBearingImpl(2,
                        new DegreeBearingImpl(0)));
        waitThreeMillis();
        GPSFixMovingImpl gpsFix4 = new GPSFixMovingImpl(
                new DegreePosition(3, 4), new MillisecondsTimePoint(
                        System.currentTimeMillis()), new KnotSpeedWithBearingImpl(2,
                        new DegreeBearingImpl(0)));
        waitThreeMillis();
        GPSFixMovingImpl gpsFix5 = new GPSFixMovingImpl(
                new DegreePosition(5, 4), new MillisecondsTimePoint(
                        System.currentTimeMillis()), new KnotSpeedWithBearingImpl(2, new DegreeBearingImpl(0)));
        track.addGPSFix(gpsFix1);
        track.addGPSFix(gpsFix2);
        track.addGPSFix(gpsFix3);
        track.addGPSFix(gpsFix4);
        track.addGPSFix(gpsFix5);
    }
    
    /**
     * Used to ensure that for the test fixes there is always a time point between the two that
     * is different from the time points of the adjacent fixes
     */
    private void waitThreeMillis() throws InterruptedException {
        Thread.sleep(3);
    }

    @Test
    public void testIterate() {
        Iterator<GPSFixMoving> i = track.getFixes().iterator();
        int count;
        for (count=0; i.hasNext(); count++) {
            i.next();
        }
        assertEquals(5, count);
    }
    
    @Test
    public void testOrdering() {
        long lastMillis = 0;
        GPSFix lastFix = null;
        boolean first = true;
        for (Iterator<GPSFixMoving> i = track.getFixes().iterator(); i.hasNext(); first = false) {
            GPSFixMoving fix = i.next();
            long millis = fix.getTimePoint().asMillis();
            if (!first) {
                assertTrue(millis > lastMillis);
                TimePoint inBetweenTimePoint = new MillisecondsTimePoint((millis+lastMillis)/2);
                assertEquals(lastFix, track.getLastFixBefore(inBetweenTimePoint));
                assertEquals(lastFix, track.getLastFixAtOrBefore(inBetweenTimePoint));
                assertEquals(fix, track.getFirstFixAfter(inBetweenTimePoint));
                assertEquals(fix, track.getFirstFixAtOrAfter(inBetweenTimePoint));

                assertEquals(lastFix, track.getLastFixAtOrBefore(lastFix.getTimePoint()));
                assertEquals(fix, track.getFirstFixAtOrAfter(fix.getTimePoint()));

                assertEquals(lastFix, track.getLastFixBefore(fix.getTimePoint()));
                assertEquals(fix, track.getLastFixAtOrBefore(fix.getTimePoint()));
                assertEquals(fix, track.getFirstFixAfter(lastFix.getTimePoint()));
                assertEquals(lastFix, track.getFirstFixAtOrAfter(lastFix.getTimePoint()));
            }
            lastMillis = millis;
            lastFix = fix;
        }
    }
    
    @Test
    public void assertEstimatedPositionBeforeStartIsStart() {
        GPSFixMoving start = track.getFixes().iterator().next();
        TimePoint oneNanoBeforeStart = new MillisecondsTimePoint(start.getTimePoint().asMillis()-1);
        assertEquals(start.getPosition(), track.getEstimatedPosition(oneNanoBeforeStart, false));
    }
    
    @Test
    public void testSimpleInterpolation() {
        long lastMillis = 0;
        GPSFix lastFix = null;
        boolean first = true;
        for (Iterator<GPSFixMoving> i = track.getFixes().iterator(); i.hasNext(); first = false) {
            GPSFixMoving fix = i.next();
            long millis = fix.getTimePoint().asMillis();
            if (!first) {
                TimePoint inBetweenTimePoint = new MillisecondsTimePoint((millis+lastMillis)/2);
                Position interpolatedPosition = track.getEstimatedPosition(inBetweenTimePoint, false);
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
        GPSFix fix = track.getLastFix();
        long millis = fix.getTimePoint().asMillis();
        GPSFix lastFix = track.getLastFixBefore(fix.getTimePoint());
        long lastMillis = lastFix.getTimePoint().asMillis();
        TimePoint afterTimePoint = new MillisecondsTimePoint(lastMillis + (lastMillis-millis));
        Position extrapolatedPosition = track.getEstimatedPosition(afterTimePoint, true);
        assertEquals(2, fix.getPosition().getDistance(extrapolatedPosition).getMeters()
                / lastFix.getPosition().getDistance(extrapolatedPosition).getMeters(), 0.00001);
    }
    
    @Test
    public void testDistanceTraveled() {
        List<Distance> distances = new ArrayList<Distance>();
        List<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
        boolean first = true;
        GPSFixMoving oldFix = null;
        for (GPSFixMoving fix : track.getFixes()) {
            fixes.add(fix);
            if (first) {
                first = false;
            } else {
                Distance d = oldFix.getPosition().getDistance(fix.getPosition());
                distances.add(d);
            }
            oldFix = fix;
        }
        for (int i=0; i<fixes.size(); i++) {
            for (int j=i; j<fixes.size(); j++) {
                double distanceSumInNauticalMiles = 0;
                for (int k=i; k<j; k++) {
                    distanceSumInNauticalMiles += distances.get(k).getNauticalMiles();
                }
                // travel fully from fix #i to fix #j and require the segment distances to sum up equal
                double nauticalMilesFromIToJ = track.getDistanceTraveled(fixes.get(i).getTimePoint(), fixes.get(j).getTimePoint())
                        .getNauticalMiles();
                assertEquals(distanceSumInNauticalMiles, nauticalMilesFromIToJ, 0.0000001);
                if (j > i) {
                    // now skip half a segment at the beginning:
                    double nauticalMilesFromHalfAfterIToJ = track.getDistanceTraveled(
                            new MillisecondsTimePoint((fixes.get(i).getTimePoint().asMillis() + fixes.get(i + 1)
                                    .getTimePoint().asMillis()) / 2), fixes.get(j).getTimePoint()).getNauticalMiles();
                    assertTrue("for i=" + i + ", j=" + j + ": " + nauticalMilesFromHalfAfterIToJ + "<"
                            + distanceSumInNauticalMiles, nauticalMilesFromHalfAfterIToJ < distanceSumInNauticalMiles);
                    if (i > 0) {
                        // now skip half a segment before the beginning:
                        double nauticalMilesFromHalfBeforeIToJ = track.getDistanceTraveled(
                                new MillisecondsTimePoint((fixes.get(i).getTimePoint().asMillis() + fixes.get(i - 1)
                                        .getTimePoint().asMillis()) / 2), fixes.get(j).getTimePoint())
                                .getNauticalMiles();
                        assertTrue(nauticalMilesFromHalfBeforeIToJ > distanceSumInNauticalMiles);
                    }
                    // now skip half a segment at the end:
                    double nauticalMilesFromIToHalfBeforeJ = track.getDistanceTraveled(
                            fixes.get(i).getTimePoint(),
                            new MillisecondsTimePoint((fixes.get(j).getTimePoint().asMillis() + fixes.get(j - 1)
                                    .getTimePoint().asMillis()) / 2)).getNauticalMiles();
                    assertTrue(nauticalMilesFromIToHalfBeforeJ < distanceSumInNauticalMiles);
                    if (j < fixes.size() - 1) {
                        // now skip half a segment before the beginning:
                        double nauticalMilesFromIToHalfAfterJ = track.getDistanceTraveled(
                                fixes.get(i).getTimePoint(),
                                new MillisecondsTimePoint((fixes.get(j).getTimePoint().asMillis() + fixes.get(j + 1)
                                        .getTimePoint().asMillis()) / 2)).getNauticalMiles();
                        assertTrue("for i=" + i + ", j=" + j + ": " + nauticalMilesFromIToHalfAfterJ + ">"
                                + distanceSumInNauticalMiles, nauticalMilesFromIToHalfAfterJ > distanceSumInNauticalMiles);
                    }
                }
            }
        }
    }
}
