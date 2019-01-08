package com.sap.sailing.domain.maneuverdetection.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.NoFixesException;
import com.sap.sailing.domain.maneuverdetection.impl.IncrementalManeuverDetectorImpl.IncrementalManeuverSpotDetectionResult;
import com.sap.sailing.domain.maneuverdetection.impl.IncrementalManeuverDetectorImpl.ManeuverDetectionResult;
import com.sap.sailing.domain.test.AbstractManeuverDetectionTestCase;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

/**
 * Tests incremental maneuver computation components and asserts that the computed maneuver data in an incremental way
 * matches the data computed within a full (non-incremental) run.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class IncrementalManeuverComputationTest extends AbstractManeuverDetectionTestCase {
    public IncrementalManeuverComputationTest() throws MalformedURLException, URISyntaxException {
    }

    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException,
            ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        URI storedUri = new URI("file:///"
                + new File("resources/event_20110609_KielerWoch-505_Race_2.mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(
                new URL("file:///" + new File("resources/event_20110609_KielerWoch-505_Race_2.txt").getCanonicalPath()),
                /* liveUri */ null, /* storedUri */ storedUri,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        OnlineTracTracBasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace(),
                new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
        getTrackedRace().recordWind(
                new WindImpl(/* position */null, new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:53:30")),
                        new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(55))),
                new WindSourceImpl(WindSourceType.WEB));
    }

    // TODO Think about how to implement an incremental douglas peucker which matches exactly the non-incremental
    // version
    @SuppressWarnings("deprecation")
    @Test
    @Ignore
    public void testIncrementalDouglasPeucker() throws NoWindException {
        Competitor competitor = getCompetitorByName("Findel");
        DynamicTrackedRaceImpl trackedRace = getTrackedRace();
        IncrementalManeuverDetectorImpl maneuverDetector = new IncrementalManeuverDetectorImpl(trackedRace, competitor,
                null);
        TrackTimeInfo trackTimeInfo = maneuverDetector.getTrackTimeInfo();
        // start from the middle
        TimePoint startTime = trackTimeInfo.getTrackEndTimePoint()
                .minus(trackTimeInfo.getTrackStartTimePoint().until(trackTimeInfo.getTrackEndTimePoint()).divide(2));
        TimePoint endTime = startTime;
        IncrementalApproximatedFixesCalculatorImpl douglasPeucker = new IncrementalApproximatedFixesCalculatorImpl(
                trackedRace, competitor);
        while (startTime.after(trackTimeInfo.getTrackStartTimePoint())) {
            douglasPeucker.approximate(startTime, endTime);
            startTime = startTime.minus(new MillisecondsDurationImpl(30000));
            endTime = endTime.plus(new MillisecondsDurationImpl(30000));
        }
        Iterable<GPSFixMoving> incrementallyApproximatedFixes = douglasPeucker
                .approximate(trackTimeInfo.getTrackStartTimePoint(), trackTimeInfo.getTrackEndTimePoint());
        ApproximatedFixesCalculatorImpl normalDouglasPeucker = new ApproximatedFixesCalculatorImpl(trackedRace,
                competitor);
        Iterable<GPSFixMoving> normallyApproximatedFixes = normalDouglasPeucker
                .approximate(trackTimeInfo.getTrackStartTimePoint(), trackTimeInfo.getTrackEndTimePoint());
        assertEquals("Incrementally calculated douglas peucker fixes differ from normally calculated fixes",
                normallyApproximatedFixes, incrementallyApproximatedFixes);
    }

    @Test
    public void testIncrementalManeuverCalculationWithoutIncrementalDouglasPeucker()
            throws NoWindException, NoFixesException {
        Competitor competitor = getCompetitorByName("Findel");
        DynamicTrackedRaceImpl trackedRace = getTrackedRace();
        IncrementalManeuverDetectorImpl maneuverDetector = new IncrementalManeuverDetectorImpl(trackedRace, competitor,
                null);
        TrackTimeInfo trackTimeInfo = maneuverDetector.getTrackTimeInfo();
        // start from the middle
        TimePoint startTime = trackTimeInfo.getTrackEndTimePoint()
                .minus(trackTimeInfo.getTrackStartTimePoint().until(trackTimeInfo.getTrackEndTimePoint()).divide(2));
        TimePoint endTime = startTime;
        ApproximatedFixesCalculatorImpl douglasPeucker = new ApproximatedFixesCalculatorImpl(trackedRace, competitor);
        int incrementalRunsCount = 1;
        ManeuverDetectionResult lastResult = new ManeuverDetectionResult(endTime, Collections.emptyList(),
                incrementalRunsCount++);
        while (startTime.after(trackTimeInfo.getTrackStartTimePoint())) {
            TrackTimeInfo mockedTrackTimeInfo = new TrackTimeInfo(startTime, endTime, endTime);
            IncrementalManeuverSpotDetectionResult detectionResult = maneuverDetector.detectManeuverSpotsIncrementally(
                    mockedTrackTimeInfo, douglasPeucker.approximate(startTime, endTime), lastResult);
            List<ManeuverSpotWithTypedManeuvers> maneuverSpots = maneuverDetector
                    .getAllManeuverSpotsWithTypedManeuversFromDetectionResultSortedByTimePoint(detectionResult);
            lastResult = new ManeuverDetectionResult(endTime, maneuverSpots, incrementalRunsCount++);
            startTime = startTime.minus(new MillisecondsDurationImpl(30000));
            endTime = endTime.plus(new MillisecondsDurationImpl(30000));
        }
        maneuverDetector.setLastManeuverDetectionResult(lastResult);
        long performanceMeasurementStartedAt = System.currentTimeMillis();
        List<Maneuver> incrementallyDetectedManeuvers = maneuverDetector.detectManeuvers();
        long millisForIncrementalManeuverDetection = System.currentTimeMillis() - performanceMeasurementStartedAt;
        ManeuverDetectorImpl normalManeuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
        performanceMeasurementStartedAt = System.currentTimeMillis();
        List<Maneuver> normallyDetectedManeuvers = normalManeuverDetector.detectManeuvers();
        long millisForNormalManeuverDetection = System.currentTimeMillis() - performanceMeasurementStartedAt;
        int performanceBenefitOfIncrementalManeuverDetectionInPercent = (int) ((1.0
                * (millisForNormalManeuverDetection - millisForIncrementalManeuverDetection)
                / millisForNormalManeuverDetection) * 100);
        assertEquals("Incrementally calculated maneuvers differ from normally calculated maneuvers",
                normallyDetectedManeuvers, incrementallyDetectedManeuvers);
        assertTrue(
                "Incremental maneuver detection was not 20% faster in detecting maneuvers incrementally, than the full maneuver detection. The actual performance benefit was: "
                        + performanceBenefitOfIncrementalManeuverDetectionInPercent + "%",
                performanceBenefitOfIncrementalManeuverDetectionInPercent >= 20);
    }

}
