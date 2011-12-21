package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.BearingCluster;
import com.sap.sailing.util.Util;

public class WindEstimationOnStoredTracksTest extends StoredTrackBasedTestWithTrackedRace {
    @Test
    public void testBearingClusterSplit() {
        BearingCluster cluster = new BearingCluster();
        for (double bearingInDegrees : new double[] { 32.31650532600039, 16.99636033752683, 37.59302174779672,
                27.2860810183163, 319.47157698009613, 325.1617832132204, 31.678409742672212, 35.00547108150359,
                23.934778873669256, 29.76599976685808, 33.19487072661667, 19.0, 33.29318052266396, 32.7371445230587,
                38.26627143611533 }) {
            cluster.add(new DegreeBearingImpl(bearingInDegrees));
        }
        BearingCluster[] splitResult = cluster.splitInTwo(/* minimumDegreeDifferenceBetweenTacks */ 15);
        assertEquals(2, splitResult.length);
        assertNotNull(splitResult[0]);
        assertNotNull(splitResult[1]);
        assertFalse(splitResult[0].isEmpty());
        assertFalse(splitResult[1].isEmpty());
        assertEquals(322, splitResult[0].getAverage().getDegrees(), 1);
        assertEquals(30, splitResult[1].getAverage().getDegrees(), 1);
    }
    
    @Test
    public void testBearingClusterAverageAcrossZero() {
        BearingCluster cluster = new BearingCluster();
        Bearing b1 = new DegreeBearingImpl(355);
        Bearing b2 = new DegreeBearingImpl(5);
        cluster.add(b1);
        cluster.add(b2);
        Bearing average = cluster.getAverage();
        assertEquals(0, average.getDegrees(), 0.00000001);
    }
    
    @Test
    public void testSuccessfulTrackedRaceCreation() throws FileNotFoundException, IOException {
        super.setUp();
        assertNotNull(getTrackedRace());
        assertEquals(Util.size(getTrackedRace().getRace().getCompetitors()), loadTracks().size());
    }

    @Test
    public void testSimpleWindEstimation() throws NoWindException, FileNotFoundException, IOException {
        super.setUp();
        Competitor hasso = getCompetitorByName("Dr.Plattner");
        GPSFixTrack<Competitor, GPSFixMoving> hassosTrack = getTrackedRace().getTrack(hasso);
        TimePoint start = hassosTrack.getFirstRawFix().getTimePoint();
        TimePoint stop = hassosTrack.getLastRawFix().getTimePoint();
        TimePoint middle = new MillisecondsTimePoint(start.asMillis() + (stop.asMillis()-start.asMillis())*3/4);
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, middle);
        assertNotNull(estimatedWindDirection);
    }
    
}
