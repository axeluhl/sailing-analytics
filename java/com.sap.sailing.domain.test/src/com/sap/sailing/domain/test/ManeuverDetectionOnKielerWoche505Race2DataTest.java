package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.NavigableSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DouglasPeucker;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class ManeuverDetectionOnKielerWoche505Race2DataTest extends KielWeek2011BasedTest {

    public ManeuverDetectionOnKielerWoche505Race2DataTest() throws MalformedURLException, URISyntaxException {
    }
    
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        super.setUp();
        super.setUp("event_20110609_KielerWoch",
                /* raceId */ "357c700a-9d9a-11e0-85be-406186cbf87c", new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        KielWeek2011BasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace());
        getTrackedRace().setWindSource(WindSource.WEB);
        getTrackedRace().recordWind(new WindImpl(/* position */ null, MillisecondsTimePoint.now(),
                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(70))), WindSource.WEB);
    }
    
    @Test
    public void testDouglasPeuckerForHasso() throws NoWindException {
        Competitor hasso = getCompetitorByName("Dr.Plattner");
        GPSFixTrack<Competitor, GPSFixMoving> hassosTrack = getTrackedRace().getTrack(hasso);
        NavigableSet<MarkPassing> hassosMarkPassings = getTrackedRace().getMarkPassings(hasso);
        DouglasPeucker<Competitor, GPSFixMoving> dp = new DouglasPeucker<Competitor, GPSFixMoving>(hassosTrack);
        Iterator<MarkPassing> hassosMarkPassingsIter = hassosMarkPassings.iterator();
        TimePoint startMarkPassing = hassosMarkPassingsIter.next().getTimePoint();
        TimePoint firstWindwardMarkPassing = hassosMarkPassingsIter.next().getTimePoint();
        GPSFix[] firstLegFineApproximation = dp.approximate(new MeterDistance(20), startMarkPassing, firstWindwardMarkPassing);
        assertNotNull(firstLegFineApproximation);
        assertEquals(9, firstLegFineApproximation.length);
        GPSFix[] firstLegCoarseApproximation = dp.approximate(new MeterDistance(50), startMarkPassing, firstWindwardMarkPassing);
        assertNotNull(firstLegCoarseApproximation);
        assertEquals(4, firstLegCoarseApproximation.length);
        TimePoint leewardGatePassing = hassosMarkPassingsIter.next().getTimePoint();
        GPSFix[] secondLegFineApproximation = dp.approximate(new MeterDistance(20), firstWindwardMarkPassing, leewardGatePassing);
        assertNotNull(secondLegFineApproximation);
        assertEquals(7, secondLegFineApproximation.length);
        GPSFix[] secondLegCoarseApproximation = dp.approximate(new MeterDistance(50), firstWindwardMarkPassing, leewardGatePassing);
        assertNotNull(secondLegCoarseApproximation);
        assertEquals(3, secondLegCoarseApproximation.length);
    }
}
