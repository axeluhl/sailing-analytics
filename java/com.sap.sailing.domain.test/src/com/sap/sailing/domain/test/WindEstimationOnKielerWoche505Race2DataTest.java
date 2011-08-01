package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.TrackedLegImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.util.Util;

public class WindEstimationOnKielerWoche505Race2DataTest extends KielerWoche2011BasedTest {

    public WindEstimationOnKielerWoche505Race2DataTest() throws MalformedURLException, URISyntaxException {
        super("357c700a-9d9a-11e0-85be-406186cbf87c");  // 505 Race 2: ID = 357c700a-9d9a-11e0-85be-406186cbf87c
    }
    
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException {
        super.setUp(new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        fixApproximateMarkPositionsForWindReadOut();
        getTrackedRace().setWindSource(WindSource.WEB);
        getTrackedRace().recordWind(new WindImpl(/* position */ null, MillisecondsTimePoint.now(),
                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(70))), WindSource.WEB);
    }
    
    /**
     * If a leg's type needs to be determined, some wind data is required to decide on upwind,
     * downwind or reaching leg. Wind information is queried by {@link TrackedLegImpl} based on
     * the marks' positions. Therefore, approximate mark positions are set here for all marks
     * of {@link #getTrackedRace()}'s courses for the time span starting at the epoch up to now.
     */
    private void fixApproximateMarkPositionsForWindReadOut() {
        TimePoint epoch = new MillisecondsTimePoint(0l);
        TimePoint now = MillisecondsTimePoint.now();
        Map<String, Position> buoyPositions = new HashMap<String, Position>();
        buoyPositions.put("K Start (left)", new DegreePosition(54.497439439999994, 10.205943000000001));
        buoyPositions.put("K Start (right)", new DegreePosition(54.500209999999996, 10.20206472));
        buoyPositions.put("K Mark4 (right)", new DegreePosition(54.499422999999986, 10.200381692));
        buoyPositions.put("K Mark4 (left)", new DegreePosition(54.498954999999995, 10.200982));
        buoyPositions.put("K Mark1", new DegreePosition(54.489738990000006, 10.17079423000015));
        buoyPositions.put("K Finish (left)", new DegreePosition(54.48918199999999, 10.17003714));
        buoyPositions.put("K Finish (right)", new DegreePosition(54.48891756, 10.170632146666675));
        for (Waypoint w : getTrackedRace().getRace().getCourse().getWaypoints()) {
            for (Buoy buoy : w.getBuoys()) {
                getTrackedRace().getTrack(buoy).addGPSFix(new GPSFixImpl(buoyPositions.get(buoy.getName()), epoch));
                getTrackedRace().getTrack(buoy).addGPSFix(new GPSFixImpl(buoyPositions.get(buoy.getName()), now));
            }
        }
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
        assertTrue(getTrackedRace().getTrack(getCompetitorByName("Köchlin")).hasDirectionChange(middle, /* minimumDegreeDifference */ 30.));
        assertTrue(getTrackedRace().getTrack(getCompetitorByName("Neulen")).hasDirectionChange(middle, /* minimumDegreeDifference */ 30.));
        assertTrue(getTrackedRace().getTrack(getCompetitorByName("Findel")).hasDirectionChange(middle, /* minimumDegreeDifference */ 30.));
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, middle);
        assertNotNull(estimatedWindDirection);
        assertEquals(245., estimatedWindDirection.getFrom().getDegrees(), 3.); // expect wind from 241 +/- 3 degrees
    }
}
