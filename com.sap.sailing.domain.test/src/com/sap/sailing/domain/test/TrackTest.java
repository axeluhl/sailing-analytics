package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.NanosecondTimePoint;
import com.sap.sailing.domain.base.impl.SpeedImpl;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class TrackTest {
    private DynamicTrackImpl<Boat, GPSFixMoving> track;

    @Before
    public void setUp() {
        track = new DynamicTrackImpl<Boat, GPSFixMoving>(new BoatImpl("MyFirstBoat",
                new BoatClassImpl("505")));
        GPSFixMovingImpl gpsFix1 = new GPSFixMovingImpl(new DegreePosition(1, 2), new NanosecondTimePoint(System.nanoTime()), 
                new SpeedImpl(1, 90));
        GPSFixMovingImpl gpsFix2 = new GPSFixMovingImpl(new DegreePosition(1, 3), new NanosecondTimePoint(System.nanoTime()), 
                new SpeedImpl(1, 90));
        GPSFixMovingImpl gpsFix3 = new GPSFixMovingImpl(new DegreePosition(1, 4), new NanosecondTimePoint(System.nanoTime()), 
                new SpeedImpl(2, 0));
        GPSFixMovingImpl gpsFix4 = new GPSFixMovingImpl(new DegreePosition(3, 4), new NanosecondTimePoint(System.nanoTime()), 
                new SpeedImpl(2, 0));
        GPSFixMovingImpl gpsFix5 = new GPSFixMovingImpl(new DegreePosition(5, 4), new NanosecondTimePoint(System.nanoTime()), 
                new SpeedImpl(2, 0));
        track.addGPSFix(gpsFix1);
        track.addGPSFix(gpsFix2);
        track.addGPSFix(gpsFix3);
        track.addGPSFix(gpsFix4);
        track.addGPSFix(gpsFix5);
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
        long lastNanos = 0;
        GPSFix lastFix = null;
        boolean first = true;
        for (Iterator<GPSFixMoving> i = track.getFixes().iterator(); i.hasNext(); first = false) {
            GPSFixMoving fix = i.next();
            long nanos = fix.getTimePoint().asNanos();
            if (!first) {
                assertTrue(nanos > lastNanos);
                TimePoint inBetweenTimePoint = new NanosecondTimePoint((nanos+lastNanos)/2);
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
            lastNanos = nanos;
            lastFix = fix;
        }
    }
    
    @Test
    public void testSimpleInterpolation() {
        long lastNanos = 0;
        GPSFix lastFix = null;
        boolean first = true;
        for (Iterator<GPSFixMoving> i = track.getFixes().iterator(); i.hasNext(); first = false) {
            GPSFixMoving fix = i.next();
            long nanos = fix.getTimePoint().asNanos();
            if (!first) {
                TimePoint inBetweenTimePoint = new NanosecondTimePoint((nanos+lastNanos)/2);
                Position interpolatedPosition = track.getEstimatedPosition(inBetweenTimePoint);
                
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
            lastNanos = nanos;
            lastFix = fix;
        }
    }
}
