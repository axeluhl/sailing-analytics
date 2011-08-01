package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.util.Util;

public class WindEstimationOnKielerWoche505Race2DataTest extends KielerWoche2011BasedTest {

    public WindEstimationOnKielerWoche505Race2DataTest() throws MalformedURLException, URISyntaxException {
        super("357c700a-9d9a-11e0-85be-406186cbf87c");  // 505 Race 2: ID = 357c700a-9d9a-11e0-85be-406186cbf87c
    }
    
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException {
        super.setUp(new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
    }
    
    @Test
    public void testSetUp() {
        assertNotNull(getTrackedRace());
        assertTrue(Util.size(getTrackedRace().getTrack(getCompetitorByName("Dr.Plattner")).getFixes()) > 1000);
    }

    @Test
    public void testSimpleWindEstimation() throws NoWindException {
        Competitor hasso = getCompetitorByName("Dr.Plattner");
        GPSFixTrack<Competitor, GPSFixMoving> hassosTrack = getTrackedRace().getTrack(hasso);
        TimePoint start = hassosTrack.getFirstRawFix().getTimePoint();
        TimePoint stop = hassosTrack.getLastRawFix().getTimePoint();
        TimePoint middle = new MillisecondsTimePoint(start.asMillis() + (stop.asMillis()-start.asMillis())*3/4);
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, middle);
        assertNotNull(estimatedWindDirection);
    }
}
