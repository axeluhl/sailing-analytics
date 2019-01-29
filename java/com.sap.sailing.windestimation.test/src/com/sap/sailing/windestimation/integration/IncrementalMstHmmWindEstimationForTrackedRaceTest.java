package com.sap.sailing.windestimation.integration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.impl.IncrementalManeuverDetectorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorWithEstimationDataSupportDecoratorImpl;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.windestimation.ManeuverBasedWindEstimationComponentImpl;
import com.sap.sailing.windestimation.ManeuverClassificationsAggregatorFactory;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.WindQuality;
import com.sap.sailing.windestimation.data.transformer.CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.ClassPathReadOnlyModelStore;
import com.sap.sailing.windestimation.preprocessing.RaceElementsFilteringPreprocessingPipelineImpl;
import com.sap.sailing.windestimation.windinference.DummyBasedTwsCalculatorImpl;
import com.sap.sailing.windestimation.windinference.MiddleCourseBasedTwdCalculatorImpl;
import com.sap.sailing.windestimation.windinference.WindTrackCalculatorImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class IncrementalMstHmmWindEstimationForTrackedRaceTest extends OnlineTracTracBasedTest {

    protected final SimpleDateFormat dateFormat;
    private final WindEstimationFactoryServiceImpl windEstimationFactoryService;
    private ClassPathReadOnlyModelStore modelStore;

    public IncrementalMstHmmWindEstimationForTrackedRaceTest()
            throws MalformedURLException, URISyntaxException, ModelPersistenceException {
        dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2")); // will result in CEST
        windEstimationFactoryService = new WindEstimationFactoryServiceImpl();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        modelStore = new ClassPathReadOnlyModelStore("trained_wind_estimation_models", getClass().getClassLoader());
        windEstimationFactoryService.importAllModelsFromModelStore(modelStore);
        if (!windEstimationFactoryService.isReady()) {
            modelStore = new ClassPathReadOnlyModelStore("/trained_wind_estimation_models",
                    getClass().getClassLoader());
            windEstimationFactoryService.importAllModelsFromModelStore(modelStore);
            System.err.println("bad1");
            if (!windEstimationFactoryService.isReady()) {
                System.err.println("bad2");
                InputStream in = getClass().getResourceAsStream("trained_wind_estimation_models");
                if (in == null) {
                    System.err.println("bad3");
                    in = getClass().getResourceAsStream("/trained_wind_estimation_models");
                    if (in == null) {
                        System.err.println("bad4");
                        in = getClass().getResourceAsStream(
                                "trained_wind_estimation_models/Serialization.modelForDistanceBasedTwdDeltaStdRegressor.IncrementalSingleDimensionPolynomialRegressor-DistanceBasedTwdTransitionRegressorFrom0.0To80.0.clf");
                        if (in == null) {
                            System.err.println("bad5");
                            in = getClass().getResourceAsStream(
                                    "/trained_wind_estimation_models/Serialization.modelForDistanceBasedTwdDeltaStdRegressor.IncrementalSingleDimensionPolynomialRegressor-DistanceBasedTwdTransitionRegressorFrom0.0To80.0.clf");
                            if (in == null) {
                                System.err.println("very baaad");
                            }
                        }
                    }
                }
            }
        }
    }

    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException,
            ParseException, SubscriberInitializationException, CreateModelException {
        modelStore = new ClassPathReadOnlyModelStore("trained_wind_estimation_models", getClass().getClassLoader());
        windEstimationFactoryService.importAllModelsFromModelStore(modelStore);
        if (!windEstimationFactoryService.isReady()) {
            modelStore = new ClassPathReadOnlyModelStore("/trained_wind_estimation_models",
                    getClass().getClassLoader());
            windEstimationFactoryService.importAllModelsFromModelStore(modelStore);
            System.err.println("bad11");
            if (!windEstimationFactoryService.isReady()) {
                System.err.println("bad22");
                InputStream in = getClass().getResourceAsStream("trained_wind_estimation_models");
                if (in == null) {
                    System.err.println("bad33");
                    in = getClass().getResourceAsStream("/trained_wind_estimation_models");
                    if (in == null) {
                        System.err.println("bad44");
                        in = getClass().getResourceAsStream(
                                "trained_wind_estimation_models/Serialization.modelForDistanceBasedTwdDeltaStdRegressor.IncrementalSingleDimensionPolynomialRegressor-DistanceBasedTwdTransitionRegressorFrom0.0To80.0.clf");
                        if (in == null) {
                            System.err.println("bad55");
                            in = getClass().getResourceAsStream(
                                    "/trained_wind_estimation_models/Serialization.modelForDistanceBasedTwdDeltaStdRegressor.IncrementalSingleDimensionPolynomialRegressor-DistanceBasedTwdTransitionRegressorFrom0.0To80.0.clf");
                            if (in == null) {
                                System.err.println("very baaad6");
                            }
                        }
                    }
                }
            }
        }
        // assertTrue("Wind estimation models are empty", windEstimationFactoryService.isReady());
        super.setUp();
        URI storedUri = new URI("file:///"
                + new File("resources/event_20110609_KielerWoch-505_Race_2.mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(
                new URL("file:///" + new File("resources/event_20110609_KielerWoch-505_Race_2.txt").getCanonicalPath()),
                /* liveUri */ null, /* storedUri */ storedUri,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        OnlineTracTracBasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace(),
                new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
        getTrackedRace()
                .setWindEstimation(windEstimationFactoryService.createIncrementalWindEstimationTrack(getTrackedRace()));
        getTrackedRace().waitForManeuverDetectionToFinish();
    }

    // TODO bugfix wind estimation
    @Test
    public void testIncrementalMstHmmWindEstimationForTrackedRace() throws NoWindException, ModelPersistenceException {
        modelStore = new ClassPathReadOnlyModelStore("trained_wind_estimation_models", getClass().getClassLoader());
        windEstimationFactoryService.importAllModelsFromModelStore(modelStore);
        if (!windEstimationFactoryService.isReady()) {
            modelStore = new ClassPathReadOnlyModelStore("/trained_wind_estimation_models",
                    getClass().getClassLoader());
            windEstimationFactoryService.importAllModelsFromModelStore(modelStore);
            System.err.println("bad111");
            if (!windEstimationFactoryService.isReady()) {
                System.err.println("bad222");
                InputStream in = getClass().getResourceAsStream("trained_wind_estimation_models");
                if (in == null) {
                    System.err.println("bad333");
                    in = getClass().getResourceAsStream("/trained_wind_estimation_models");
                    if (in == null) {
                        System.err.println("bad444");
                        in = getClass().getResourceAsStream(
                                "trained_wind_estimation_models/Serialization.modelForDistanceBasedTwdDeltaStdRegressor.IncrementalSingleDimensionPolynomialRegressor-DistanceBasedTwdTransitionRegressorFrom0.0To80.0.clf");
                        if (in == null) {
                            System.err.println("bad555");
                            in = getClass().getResourceAsStream(
                                    "/trained_wind_estimation_models/Serialization.modelForDistanceBasedTwdDeltaStdRegressor.IncrementalSingleDimensionPolynomialRegressor-DistanceBasedTwdTransitionRegressorFrom0.0To80.0.clf");
                            if (in == null) {
                                System.err.println("very baaad66");
                                throw new ModelPersistenceException("baaaaad");
                            }
                        }
                    }
                }
            }
        }
        DynamicTrackedRaceImpl trackedRace = getTrackedRace();
        WindTrack estimatedWindTrackOfTrackedRace = trackedRace
                .getOrCreateWindTrack(new WindSourceImpl(WindSourceType.MANEUVER_BASED_ESTIMATION));
        List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracks = new ArrayList<>();
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            IncrementalManeuverDetectorImpl maneuverDetector = new IncrementalManeuverDetectorImpl(trackedRace,
                    competitor, null);
            ManeuverDetectorWithEstimationDataSupportDecoratorImpl maneuverDetectorWithEstimationDataSupportDecorator = new ManeuverDetectorWithEstimationDataSupportDecoratorImpl(
                    maneuverDetector, null);
            List<CompleteManeuverCurve> maneuverCurves = maneuverDetectorWithEstimationDataSupportDecorator
                    .detectCompleteManeuverCurves();
            List<CompleteManeuverCurveWithEstimationData> completeManeuverCurvesWithEstimationData = maneuverDetectorWithEstimationDataSupportDecorator
                    .getCompleteManeuverCurvesWithEstimationData(maneuverCurves);
            CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrack = new CompetitorTrackWithEstimationData<>(
                    trackedRace.getTrackedRegatta().getRegatta().getName(), trackedRace.getRace().getName(),
                    competitor.getName(), trackedRace.getBoatOfCompetitor(competitor).getBoatClass(),
                    completeManeuverCurvesWithEstimationData, 1, null, null, null, 0, 0);
            competitorTracks.add(competitorTrack);
        }
        RaceWithEstimationData<CompleteManeuverCurveWithEstimationData> race = new RaceWithEstimationData<>(
                competitorTracks.get(0).getRegattaName(), competitorTracks.get(0).getRaceName(), WindQuality.LOW,
                competitorTracks);
        ManeuverBasedWindEstimationComponentImpl<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> targetWindEstimation = new ManeuverBasedWindEstimationComponentImpl<>(
                new RaceElementsFilteringPreprocessingPipelineImpl(false,
                        new CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer()),
                windEstimationFactoryService.maneuverClassifiersCache,
                new ManeuverClassificationsAggregatorFactory(null, modelStore, false, Long.MAX_VALUE).mstHmm(false),
                new WindTrackCalculatorImpl(new MiddleCourseBasedTwdCalculatorImpl(),
                        new DummyBasedTwsCalculatorImpl()));
        List<WindWithConfidence<Pair<Position, TimePoint>>> windFixes = targetWindEstimation.estimateWindTrack(race);
        List<Wind> targetWindFixes = new ArrayList<>(windFixes.size());
        for (WindWithConfidence<Pair<Position, TimePoint>> windFix : windFixes) {
            Wind wind = windFix.getObject();
            targetWindFixes.add(wind);
            System.out.println("Target: " + wind.getTimePoint() + " " + wind.getPosition() + " "
                    + Math.round(wind.getFrom().getDegrees()));
        }
        List<Wind> estimatedWindFixes = new ArrayList<>();
        estimatedWindTrackOfTrackedRace.lockForRead();
        try {
            for (Wind wind : estimatedWindTrackOfTrackedRace.getFixes()) {
                estimatedWindFixes.add(wind);
                System.out.println("Estimated: " + wind.getTimePoint() + " " + wind.getPosition() + " "
                        + Math.round(wind.getFrom().getDegrees()));
            }
        } finally {
            estimatedWindTrackOfTrackedRace.unlockAfterRead();
        }
        Comparator<Wind> windFixesComparator = new Comparator<Wind>() {

            @Override
            public int compare(Wind o1, Wind o2) {
                return o1.getTimePoint().compareTo(o2.getTimePoint());
            }
        };
        Collections.sort(targetWindFixes, windFixesComparator);
        Collections.sort(estimatedWindFixes, windFixesComparator);

        Map<Pair<Position, TimePoint>, Wind> targetWindFixesMap = new TreeMap<>(
                new TimePointAndPositionWithToleranceComparator());
        for (Wind wind : targetWindFixes) {
            targetWindFixesMap.put(new Pair<>(wind.getPosition(), wind.getTimePoint()), wind);
        }
        Map<Pair<Position, TimePoint>, Wind> estimatedWindFixesMap = new TreeMap<>(
                new TimePointAndPositionWithToleranceComparator());
        for (Wind wind : estimatedWindFixes) {
            Pair<Position, TimePoint> relativeTo = new Pair<>(wind.getPosition(), wind.getTimePoint());
            estimatedWindFixesMap.put(relativeTo, wind);
            Wind targetWind = targetWindFixesMap.get(relativeTo);
            if (targetWind == null) {
                System.out.println("Not present in target: " + wind.getTimePoint() + " " + wind.getPosition());
            } else if (targetWind.getBearing().getDifferenceTo(wind.getBearing()).abs().getDegrees() > 10) {
                System.out.println("TWD difference: " + wind.getTimePoint() + " " + wind.getPosition() + " "
                        + targetWind.getBearing().getDifferenceTo(wind.getBearing()).abs().getDegrees() + " deg");
            }
        }
        for (Wind wind : targetWindFixes) {
            if (!estimatedWindFixesMap.containsKey(new Pair<>(wind.getPosition(), wind.getTimePoint()))) {
                System.out.println("Not present in estimated: " + wind.getTimePoint() + " " + wind.getPosition());
            }
        }

        assertEquals("Number of estimated fixes is not equal to the number of target wind fixes",
                targetWindFixes.size(), estimatedWindFixes.size());
        assertEquals(
                "Different wind tracks estimated by IncrementalMstHmmWindEstimationForTrackedRace and ManeuverBasedWindEstimationComponentImpl",
                targetWindFixes, estimatedWindFixes);
    }

}
