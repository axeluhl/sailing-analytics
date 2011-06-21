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

    /**
     * If the wind track has areas with no data, and wind information is requested for such an interval,
     * it is essential to still average over the {@link #AVERAGING_INTERVAL_MILLIS} interval, even if the
     * interval is further away than {@link #AVERAGING_INTERVAL_MILLIS}.
     */
    @Test
    public void testAveragingOfSparseWindTrack() {
        WindTrack track = new WindTrackImpl(AVERAGING_INTERVAL_MILLIS);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind1 = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(0)));
        Wind wind2 = new WindImpl(pos, new MillisecondsTimePoint(1000), new KnotSpeedWithBearingImpl(20, new DegreeBearingImpl(0)));
        Wind wind3 = new WindImpl(pos, new MillisecondsTimePoint(2000), new KnotSpeedWithBearingImpl(30, new DegreeBearingImpl(0)));
        Wind wind4 = new WindImpl(pos, new MillisecondsTimePoint(10000), new KnotSpeedWithBearingImpl(40, new DegreeBearingImpl(0)));
        Wind wind5 = new WindImpl(pos, new MillisecondsTimePoint(30000), new KnotSpeedWithBearingImpl(50, new DegreeBearingImpl(0)));
        Wind wind6 = new WindImpl(pos, new MillisecondsTimePoint(40000), new KnotSpeedWithBearingImpl(60, new DegreeBearingImpl(0)));
        Wind wind7 = new WindImpl(pos, new MillisecondsTimePoint(50000), new KnotSpeedWithBearingImpl(70, new DegreeBearingImpl(0)));
        track.add(wind1);
        track.add(wind2);
        track.add(wind3);
        track.add(wind4);
        track.add(wind5);
        track.add(wind6);
        track.add(wind7);
        
        // interval does bearely reach 20's burst because 0 has 0 length and 1000..30000 has 29000 length
        assertEquals((10+20+30+40+50)/5, track.getEstimatedWind(pos, new MillisecondsTimePoint(1)).getKnots(), 0.00000001);
        // interval uses the two fixes to the left (0, 1000)=1000 and three to the right (2000, 10000, 30000)=28000
        assertEquals((10+20+30+40+50)/5, track.getEstimatedWind(pos, new MillisecondsTimePoint(1001)).getKnots(), 0.00000001);
        // in the middle of the "hole", fetches (0, 1000, 2000, 10000)=10000 and (30000, 40000)=10000, so 20000ms worth of wind
        assertEquals((10+20+30+40+50+60)/6, track.getEstimatedWind(pos, new MillisecondsTimePoint(11000)).getKnots(), 0.00000001);
        // right of the middle of the "hole", fetches (0, 1000, 2000, 10000)=10000 and (30000, 40000, 50000)=20000
        assertEquals((10+20+30+40+50+60+70)/7, track.getEstimatedWind(pos, new MillisecondsTimePoint(20500)).getKnots(), 0.00000001);
    }
    
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
        // expectation: take two from left (because they are closer than AVERAGING_INTERVAL_MILLIS apart), one from right side:
        assertEquals((wind1.getKnots() + wind2.getKnots() + wind3.getKnots()) / 3, result.getKnots(), 0.000000001);
        assertEquals((wind1.getBearing().getDegrees() + wind2.getBearing().getDegrees() + wind3.getBearing()
                .getDegrees()) / 3, result.getBearing().getDegrees(), 0.0000000001);
    }
}
