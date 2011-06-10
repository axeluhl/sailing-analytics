package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;

public class WindTest {
    private static final int AVERAGING_INTERVAL_MILLIS = 30000 /* 30s averaging interval */;

    @Test
    public void testSingleElementWindTrack() {
        WindTrack track = new WindTrackImpl(AVERAGING_INTERVAL_MILLIS);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(123)));
        track.add(wind);
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(0));
        assertEquals(10, estimate.getKnots(), 0.000000001);
        assertEquals(123, estimate.getBearing().getDegrees(), 0.000000001);
    }

    @Test
    public void testSingleElementExtrapolation() {
        WindTrack track = new WindTrackImpl(30000 /* 30s averaging interval */);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(123)));
        track.add(wind);
        // we only have one measurement; this should be extrapolated because it's our best guess
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(1000));
        assertEquals(10, estimate.getKnots(), 0.000000001);
        assertEquals(123, estimate.getBearing().getDegrees(), 0.000000001);
    }

    @Test
    public void testSingleElementExtrapolationBeyondThreshold() {
        WindTrack track = new WindTrackImpl(30000 /* 30s averaging interval */);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(123)));
        track.add(wind);
        // we only have one measurement; this should be extrapolated because it's our best guess even if
        // the last measurement was longer ago than our smoothening interval
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(AVERAGING_INTERVAL_MILLIS+1000));
        assertEquals(10, estimate.getKnots(), 0.000000001);
        assertEquals(123, estimate.getBearing().getDegrees(), 0.000000001);
    }

    @Test
    public void testTwoElementWindTrackSameBearing() {
        WindTrack track = new WindTrackImpl(30000 /* 30s averaging interval */);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind1 = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(100)));
        track.add(wind1);
        Wind wind2 = new WindImpl(pos, new MillisecondsTimePoint(1000), new KnotSpeedWithBearingImpl(20, new DegreeBearingImpl(100)));
        track.add(wind2);
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(2000));
        assertEquals(15, estimate.getKnots(), 0.000000001);
        assertEquals(100, estimate.getBearing().getDegrees(), 0.00000001);
    }

    @Test
    public void testTwoElementWindTrackDifferentBearing() {
        WindTrack track = new WindTrackImpl(30000 /* 30s averaging interval */);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind1 = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(110)));
        track.add(wind1);
        Wind wind2 = new WindImpl(pos, new MillisecondsTimePoint(1000), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(100)));
        track.add(wind2);
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(2000));
        assertEquals(10, estimate.getKnots(), 0.000000001);
        assertEquals(105, estimate.getBearing().getDegrees(), 0.00000001);
    }
    
    @Test
    public void testUsingNewerThanRequestedIfCloserThanOlder() throws ParseException {
        /*
           Imagine the following wind measurements:
           
        2009-07-11T13:45:00.000+0200@null: 10.0kn from 278.0� avg(30000ms): 2009-07-11T13:45:00.000+0200@null: 10.0kn from 278.0�
        2009-07-11T13:45:05.000+0200@null: 10.0kn from 265.0� avg(30000ms): 2009-07-11T13:45:05.000+0200@null: 10.0kn from 269.0�
        2009-07-12T17:31:40.000+0200@null: 10.0kn from 260.0� avg(30000ms): 2009-07-12T17:31:40.000+0200@null: 10.0kn from 260.0�
        
           Now assume a query for 2009-07-12T17:30:00 which is closest to the newest entry but (much) more than
           the averaging interval after the previous entry (2009-07-11T13:45:05.000). This test ensures that
           the WindTrack uses the newer entry even though it's after the time point requested because it's
           much closer, and the previous entry would be out of the averaging interval anyway.
        */
        SimpleDateFormat df = new SimpleDateFormat("yyyy-DD-mm'T'hh:mm:ss");
        Wind wind1 = new WindImpl(null, new MillisecondsTimePoint(df.parse("2009-07-11T13:45:00").getTime()),
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(98)));
        Wind wind2 = new WindImpl(null, new MillisecondsTimePoint(df.parse("2009-07-11T13:45:05").getTime()),
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(85)));
        Wind wind3 = new WindImpl(null, new MillisecondsTimePoint(df.parse("2009-07-11T17:31:40").getTime()),
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(80)));
        WindTrack track = new WindTrackImpl(/* millisecondsOverWhichToAverage */ 30000);
        track.add(wind1);
        track.add(wind2);
        track.add(wind3);
        TimePoint timePoint = new MillisecondsTimePoint(df.parse("2009-07-11T17:31:38").getTime());
        Wind result = track.getEstimatedWind(null, timePoint);
        assertEquals(wind3.getKnots(), result.getKnots(), 0.000000001);
        assertEquals(wind3.getBearing().getDegrees(), result.getBearing().getDegrees(), 0.0000000001);
    }
}
