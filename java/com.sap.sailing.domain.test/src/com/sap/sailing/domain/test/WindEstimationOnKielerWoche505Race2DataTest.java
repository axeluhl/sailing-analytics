package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.util.Util;

public class WindEstimationOnKielerWoche505Race2DataTest extends KielWeek2011BasedTest {

    public WindEstimationOnKielerWoche505Race2DataTest() throws MalformedURLException, URISyntaxException {
    }
    
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        super.setUp();
        super.setUp(/* raceId */ "357c700a-9d9a-11e0-85be-406186cbf87c",
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        KielWeek2011BasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace());
        getTrackedRace().setWindSource(WindSource.WEB);
        getTrackedRace().recordWind(new WindImpl(/* position */ null, MillisecondsTimePoint.now(),
                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(70))), WindSource.WEB);
    }
    
    @Test
    public void testSetUp() {
        assertNotNull(getTrackedRace());
        assertTrue(Util.size(getTrackedRace().getTrack(getCompetitorByName("Dr.Plattner")).getFixes()) > 1000);
    }

    @Test
    public void testSimpleWindEstimation() throws NoWindException {
        // at this point in time, a few boats are still going downwind, a few have passed the downwind
        // mark and are already going upwind again, and Lehmann is tacking, hence has a direction change.
        TimePoint middle = new MillisecondsTimePoint(1308839250105l);
        assertTrue(getTrackedRace().getTrack(getCompetitorByName("Lehmann")).hasDirectionChange(middle, /* minimumDegreeDifference */ 30.));
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, middle);
        assertNotNull(estimatedWindDirection);
        assertEquals(241., estimatedWindDirection.getFrom().getDegrees(), 3.); // expect wind from 241 +/- 3 degrees
    }
    
    @Test
    public void testAnotherSimpleWindEstimation() throws NoWindException {
        // at this point in time, most boats are already going upwind again, and Köchlin, Neulen and Findel are tacking,
        // hence have a direction change.
        TimePoint middle = new MillisecondsTimePoint(1308839492322l);
        assertTrue(getTrackedRace().getTrack(getCompetitorByName("K.chlin")).hasDirectionChange(middle, /* minimumDegreeDifference */ 30.));
        assertTrue(getTrackedRace().getTrack(getCompetitorByName("Neulen")).hasDirectionChange(middle, /* minimumDegreeDifference */ 30.));
        assertTrue(getTrackedRace().getTrack(getCompetitorByName("Findel")).hasDirectionChange(middle, /* minimumDegreeDifference */ 30.));
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, middle);
        assertNotNull(estimatedWindDirection);
        assertEquals(245., estimatedWindDirection.getFrom().getDegrees(), 3.); // expect wind from 241 +/- 3 degrees
    }
}
