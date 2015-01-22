package com.sap.sailing.polars.windestimation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.confidence.impl.ScalableDouble;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.ScalableWind;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.windestimation.ManeuverBasedWindEstimationTrackImpl.ManeuverClassification;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.util.kmeans.Cluster;
import com.sap.sse.util.kmeans.KMeansMappingClusterer;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class TestWindEstimationFromManeuversOnAFew505Races extends OnlineTracTracBasedTest {

    public TestWindEstimationFromManeuversOnAFew505Races() throws URISyntaxException, MalformedURLException {
        super();
    }

    public void setUp(final String fileBaseName) throws MalformedURLException, IOException, InterruptedException, URISyntaxException,
            ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        URI storedUri = new URI("file:///"+new File("../com.sap.sailing.domain.test/resources/"+fileBaseName+".mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(new URL("file:///"+new File("../com.sap.sailing.domain.test/resources/"+fileBaseName+".txt").getCanonicalPath()),
                /* liveUri */ null, /* storedUri */ storedUri,
                new ReceiverType[] { ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS }); // only the tracks; no mark positions, no wind, no mark passings
    }

    @Test
    public void testWindEstimationFromManeuversOn505KW2011Race2() throws MalformedURLException, IOException,
            InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException,
            CreateModelException, NotEnoughDataHasBeenAddedException {
        setUp("event_20110609_KielerWoch-505_Race_2");
        Wind average = getManeuverBasedAverageWind();
        assertEquals(235, average.getFrom().getDegrees(), 5.0); // wind in this race was from 075deg on average
    }

    @Ignore("The test is currently still red because the clustering doesn't work; see bug 1562 comment #8")
    @Test
    public void testWindEstimationFromManeuversOn505KW2011Race3() throws MalformedURLException, IOException,
            InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException,
            CreateModelException, NotEnoughDataHasBeenAddedException {
        setUp("event_20110609_KielerWoch-505_Race_3");
        Wind average = getManeuverBasedAverageWind();
        assertEquals(245, average.getFrom().getDegrees(), 5.0); // wind in this race was from 075deg on average
    }

    @Ignore("The test is currently still red because the clustering doesn't work; see bug 1562 comment #8")
    @Test
    public void testWindEstimationFromManeuversOn505KW2011Race4() throws MalformedURLException, IOException,
            InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException,
            CreateModelException, NotEnoughDataHasBeenAddedException {
        setUp("event_20110609_KielerWoch-505_Race_4");
        Wind average = getManeuverBasedAverageWind();
        assertEquals(275, average.getFrom().getDegrees(), 5.0); // wind in this race was from 075deg on average
    }
    
    @Test
    public void testTwoDimensionalClustering() throws NotEnoughDataHasBeenAddedException, MalformedURLException,
            IOException, InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException,
            CreateModelException {
        setUp("event_20110609_KielerWoch-505_Race_3");
        ManeuverBasedWindEstimationTrackImpl windTrack = new ManeuverBasedWindEstimationTrackImpl(new PolarDataServiceImpl(Executors.newFixedThreadPool(4)),
                getTrackedRace(), /* millisecondsOverWhichToAverage */ 30000, /* waitForLatest */ true);
        final Map<Maneuver, Competitor> maneuvers = windTrack.getAllManeuvers(/* waitForLatest */ true);
        final int numberOfClusters = 16;
        KMeansMappingClusterer<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> clusterer =
                new KMeansMappingClusterer<>(numberOfClusters,
                        windTrack.getManeuverClassifications(maneuvers),
                        (mc)->new ScalableBearingAndScalableDouble(mc.getMiddleManeuverCourse(), mc.getManeuverAngleDeg()), // maps maneuver classification to cluster metric
                        // use an evenly distributed set of cluster seeds for clustering wind direction estimations
                        Stream.concat(IntStream.range(0, numberOfClusters/2).mapToObj((i)->
                            new Pair<>(new DegreeBearingImpl(((double) i)*360./(double) numberOfClusters/2), 45.)),
                            IntStream.range(0, numberOfClusters/2).mapToObj((i)->
                                new Pair<>(new DegreeBearingImpl(((double) i)*360./(double) numberOfClusters/2), -45.))));
        final Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters = clusterer.getClusters();
        assertEquals(16, clusters.size());
        
        // Now work towards identifying the two tack clusters
        List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clustersSortedByAverageTackLikelihood =
                clusters.stream()
                .sorted((c1, c2) -> (int) -Math.signum(getAverageLikelihood(c1, ManeuverType.TACK)
                        - getAverageLikelihood(c2, ManeuverType.TACK))).collect(Collectors.toList());
        // expecting a wind direction that is from around 245deg, +/- 10deg
        // TODO compute average weighted by the likelihood of being a tack, thereby largely suppressing non-tack maneuvers in cluster
        assertEquals(245., getWeightedAverageMiddleManeuverCOG(clustersSortedByAverageTackLikelihood.get(0)), 10.);
        assertEquals(245., getWeightedAverageMiddleManeuverCOG(clustersSortedByAverageTackLikelihood.get(1)), 10.);
    }

    private double getWeightedAverageMiddleManeuverCOG(
            Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> cluster) {
        double weightedMiddleCOGDegSum = 0;
        double weightSum = 0;
        for (ManeuverClassification e : cluster) {
            final double weight = e.getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType.TACK).getA();
            weightedMiddleCOGDegSum += weight * e.getMiddleManeuverCourse().getDegrees();
            weightSum += weight;
        }
        return weightedMiddleCOGDegSum / weightSum;
    }

    private double getAverageLikelihood(
            Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> cluster,
            ManeuverType maneuverType) {
        return cluster.stream().mapToDouble((mc)->mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(maneuverType).getA()).average().getAsDouble();
    }
    
    private Wind getManeuverBasedAverageWind() throws NotEnoughDataHasBeenAddedException {
        ManeuverBasedWindEstimationTrackImpl windTrack = new ManeuverBasedWindEstimationTrackImpl(new PolarDataServiceImpl(Executors.newFixedThreadPool(4)),
                getTrackedRace(), /* millisecondsOverWhichToAverage */ 30000, /* waitForLatest */ true);
        ScalableWind windSum = null;
        int count = 0;
        windTrack.lockForRead();
        try {
            for (Wind wind : windTrack.getFixes()) {
                final ScalableWind scalableWind = new ScalableWind(wind, /* useSpeed */ true);
                if (windSum == null) {
                    windSum = scalableWind;
                } else {
                    windSum = windSum.add(scalableWind);
                }
                count++;
            }
        } finally {
            windTrack.unlockAfterRead();
        }
        Wind average = windSum.divide(count);
        return average;
    }
}
