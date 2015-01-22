package com.sap.sailing.polars.windestimation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.DoublePair;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableSpeed;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.kmeans.Cluster;
import com.sap.sse.util.kmeans.KMeansMappingClusterer;

/**
 * Implements a wind estimation based on maneuver classifications.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ManeuverBasedWindEstimationTrackImpl extends WindTrackImpl {
    private static final long serialVersionUID = -764156498143531475L;
    
    /**
     * Using a fairly low base confidence for this estimation wind track. It shall only serve as a
     * default and should be superseded by other wind sources easily.
     */
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;

    /**
     * We're trying to find the jibe cluster based on a candidate for the tack cluster. This constant tells how many degrees
     * the jibe cluster may be off from the reversed tack cluster's centroid in order to be accepted as jibe cluster candidate.
     */
    private static final double THRESHOLD_OPPOSITE_CLUSTER_DIFFERENCE_DEGREES = 20;
    
    private final PolarDataService polarService;

    private final TrackedRace trackedRace;

    /**
     * Keeps the clusters that were used during the last {@link #analyzeRace(boolean)} run; this is for debugging, mostly,
     * and could be removed/commented again when this class works as expected.
     */
    private final Set<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> clusters;

    /**
     * References one element of {@link #clutsers} that was selected as the tack cluster; this is for debugging, mostly,
     * and could be removed/commented again when this class works as expected.
     */
    private final Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> tackCluster;

    /**
     * References one element of {@link #clusters} that was selected as the jibe cluster; this is for debugging, mostly,
     * and could be removed/commented again when this class works as expected.
     */
    private final Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> jibeCluster;
    
    public ManeuverBasedWindEstimationTrackImpl(PolarDataService polarService, TrackedRace trackedRace, long millisecondsOverWhichToAverage, boolean waitForLatest)
            throws NotEnoughDataHasBeenAddedException {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */ false,
                /* nameForReadWriteLock */ ManeuverBasedWindEstimationTrackImpl.class.getName());
        this.polarService = polarService;
        this.trackedRace = trackedRace;
        Triple<Set<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>>,
               Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>,
               Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> clusterStructure = analyzeRace(waitForLatest);
        clusters = clusterStructure.getA();
        tackCluster = clusterStructure.getB();
        jibeCluster = clusterStructure.getC();
    }

    /**
     * Collects data about a maneuver that will be used to assign probabilities for maneuver types such as tack or
     * jibe, looking at a larger set of such objects and finding the most probable overall solution.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    public class ManeuverClassification {
        private final Competitor competitor;
        private final TimePoint timePoint;
        private final Position position;
        /**
         * Course change implied by the maneuver
         */
        private final double maneuverAngleDeg;
        private final SpeedWithBearing speedAtManeuverStart;
        private final Bearing middleManeuverCourse;
        private final Distance maneuverLoss;
        
        protected ManeuverClassification(Competitor competitor, Maneuver maneuver) {
            super();
            this.competitor = competitor;
            this.timePoint = maneuver.getTimePoint();
            this.position = maneuver.getPosition();
            this.maneuverAngleDeg = maneuver.getDirectionChangeInDegrees();
            this.speedAtManeuverStart = maneuver.getSpeedWithBearingBefore();
            this.middleManeuverCourse = maneuver.getSpeedWithBearingBefore().getBearing().middle(maneuver.getSpeedWithBearingAfter().getBearing());
            this.maneuverLoss = maneuver.getManeuverLoss();
        }
        
        public Competitor getCompetitor() {
            return competitor;
        }

        public TimePoint getTimePoint() {
            return timePoint;
        }

        public Position getPosition() {
            return position;
        }

        public double getManeuverAngleDeg() {
            return maneuverAngleDeg;
        }

        public SpeedWithBearing getSpeedAtManeuverStart() {
            return speedAtManeuverStart;
        }

        public Bearing getMiddleManeuverCourse() {
            return middleManeuverCourse;
        }

        public SpeedWithBearingWithConfidence<Void> getEstimatedWindSpeedAndBearing(ManeuverType maneuverType) {
            Pair<Double, SpeedWithBearingWithConfidence<Void>> likelihoodAndTWSBasedOnSpeedAndAngle = polarService.getManeuverLikelihoodAndTwsTwa(
                    getCompetitor().getBoat().getBoatClass(), getSpeedAtManeuverStart(), getManeuverAngleDeg(), maneuverType);
            final SpeedWithBearingWithConfidenceImpl<Void> result;
            // if no reasonable wind speed was found for the maneuver speed, return null
            if (likelihoodAndTWSBasedOnSpeedAndAngle.getB() == null) {
                result = null;
            } else {
                final Bearing bearing;
                final double confidence = likelihoodAndTWSBasedOnSpeedAndAngle.getB().getConfidence()
                        * likelihoodAndTWSBasedOnSpeedAndAngle.getA();
                final Speed speed = likelihoodAndTWSBasedOnSpeedAndAngle.getB().getObject();
                switch (maneuverType) {
                case TACK:
                    bearing = getMiddleManeuverCourse().reverse();
                    break;
                case JIBE:
                    bearing = getMiddleManeuverCourse();
                    break;
                default:
                    throw new IllegalStateException("Found leg type " + maneuverType + " but can only handle "
                            + LegType.UPWIND.name() + " and " + LegType.DOWNWIND.name());
                }
                result = new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(speed.getKnots(),
                        bearing), confidence,
                /* relativeTo */null);
            }
            return result;
        }
        
        public Distance getManeuverLoss() {
            return maneuverLoss;
        }

        /**
         * Computes the likelihood that the maneuver represented by this object is of the <code>type</code> requested. The polar
         * service may offer more than one possible wind condition for the given speed. In this case, the true wind angle that
         * fits the actual {@link #getManeuverAngleDeg() maneuver angle} better is chosen to judge how close the maneuver is to
         * the expected angle. The likelihood as well as the estimated true wind speed are returned.
         * 
         * @return a value between 0 and 1
         */
        public Pair<Double, SpeedWithBearingWithConfidence<Void>> getLikelihoodAndTWSBasedOnSpeedAndAngle(final ManeuverType maneuverType) {
            return polarService.getManeuverLikelihoodAndTwsTwa(getCompetitor().getBoat().getBoatClass(),
                    getSpeedAtManeuverStart(), getManeuverAngleDeg(), maneuverType);
        }
        
        @Override
        public String toString() {
            return format(this, /* id */ null);
        }

    }
    
    public String getManeuverClassificationColumnHeaders() {
        return "datapoint\tcompetitor\ttimePoint\tangleDeg\tboatSpeedKn\tcogDeg\tmiddleManeuverCourse\tlossM";
    }

    public String getManeuverClassificationColumnTypes() {
        return "infoitem\tstring\tdate\tfloat\tfloat\tfloat\tfloat\tfloat";
    }

    public String format(ManeuverClassification mc, String id) {
        DateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        final String prefix = mc.getCompetitor().getName() + "\t" + df.format(mc.getTimePoint().asDate()) + "\t" + mc.getManeuverAngleDeg()
                + "\t" + mc.getSpeedAtManeuverStart().getKnots() + "\t"
                + mc.getSpeedAtManeuverStart().getBearing().getDegrees();
        final StringBuilder result = new StringBuilder();
        if (id != null) {
            result.append("ID");
            result.append(id);
            result.append('\t');
        }
        result.append(prefix);
        result.append("\t");
        result.append(mc.getMiddleManeuverCourse().getDegrees());
        result.append("\t");
        result.append(mc.getManeuverLoss() == null ? 0.0 : mc.getManeuverLoss().getMeters());
        return result.toString();
    }

    /**
     * Fetches all the race's maneuvers and tries to find the tacks and jibes. For each such maneuver identified, a wind
     * fix will be created based on the average COG into and out of the maneuver.
     * <p>
     * 
     * All maneuvers of all competitors between start and end time are considered (usually race start and end for that
     * competitor, but may be the start of tracking, e.g., in case no course is set, and current time in case no finish
     * line mark passing is found for that competitor yet). Clustering the candidates by average COG should give two
     * clusters that are approximately 180deg apart from each other: one with the tacks and one with the jibes. A bunch
     * of other clusters will emerge for head-up/bear-away maneuvers with small course changes (one for each actual
     * tack/leg type combination), making them very unlikely candidates for maneuvers that have distinct wide expected
     * maneuver angles. Two more clusters typically emerge that hold the mark rounding maneuvers that have maneuver
     * angles similar to that of a typical tack and that would result in estimated wind directions offset by
     * approximately 90deg from the correct wind direction. Altogether this makes for eight expected clusters, two of
     * which are the interesting ones, four of which are expected to not contain likely candidates for the wide-angle
     * maneuvers (head-up/bear-away clusters), and the mark rounding clusters which to each other also tend to have a
     * 180deg offset.
     * <p>
     * 
     * Now for each cluster we check if it's the tack cluster. The tack cluster shows some characteristic features that
     * make it a little easier to identify: its maneuver angle is in most cases wider than that of the jibes, resulting
     * in two well-defined maneuver angle clusters (one for port-to-starboard and one for starboard-to-port tacks).
     * There are usually no other maneuvers with similar middle COG because boats can't sail straight against the wind.
     * <p>
     * 
     * We evaluate each cluster's likelihood of being the tack cluster by for each of its maneuvers adding up the
     * likelihood of that maneuver being a tack, solely based on its maneuver angle, as judged by the polar service. If
     * a cluster exists that has approximately the opposite average middle COG (the jibe cluster candidate), the
     * likelihoods for its maneuvers being a jibe are added. (TODO adding is statistically not adequate here; however,
     * multiplying hundreds if not thousands of probability values would easily lead to a double value going to 0.0
     * quickly. But is adding adequate to estimate the cluster having the maximum likelihood of being the tack cluster?
     * We'd only need a monotonous function.) Finally, the resulting likelihood is multiplied by how likely the speed
     * ratio between the tack and jibe cluster candidates is, based on the polar diagram. For this task, the cluster's
     * maneuvers' average speed (weighted by each maneuver's likelihood of being a tack) is divided by that of the jibe
     * cluster's maneuvers' average speed (weighted by each maneuver's likelihood of being a jibe) and assigned a
     * likelihood by the polar service.
     * <p>
     * 
     * The cluster that has the highest likelihood of being the tack cluster is selected. The confidence of the choice
     * depends on how clearly this cluster was the favorite among all clusters. For each of its maneuvers a wind fix is
     * generated with a confidence defined by the confidence of having picked the correct cluster as the tack cluster,
     * multiplied with the likelihood of the maneuver being a tack (as judged by the polar service). Similarly, each
     * maneuver from the jibe cluster is turned into a wind fix whose confidence results from the likelihood of the
     * maneuver being a jibe (based on the polar service's judgement) and the general confidence of the correct jibe
     * cluster having been selected.
     * <p>
     * 
     * One benefit of looking primarily for the tacks is that boats cannot sail straight against the wind. Therefore,
     * there usually are almost no maneuvers that have the same middle COG with a maneuver angle that resembles that of
     * the other maneuvers in the cluster. This way, the tack cluster will show two cleanly separated clusters along the
     * maneuver angle dimension that show low variance each, compared to all other clusters.
     * <p>
     * 
     * TODO with enough candidates at hand, apply temporal and spatial segmentation to account for heterogeneous wind fields
     */
    private Triple<Set<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>>,
                   Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>,
                   Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> analyzeRace(boolean waitForLatest) throws NotEnoughDataHasBeenAddedException {
        final Map<Maneuver, Competitor> maneuvers = getAllManeuvers(waitForLatest);
        // cluster into eight clusters by middle COG first, then aggregate tack likelihoods for each, and jibe likelihoods for opposite cluster
        final int numberOfClusters = 8;
        KMeansMappingClusterer<ManeuverClassification, DoublePair, Bearing, ScalableBearing> clusterer =
                new KMeansMappingClusterer<>(numberOfClusters,
                        getManeuverClassifications(maneuvers),
                        (mc)->new ScalableBearing(mc.getMiddleManeuverCourse()), // maps maneuver classification to cluster metric
                        // use an evenly distributed set of cluster seeds for clustering wind direction estimations
                        IntStream.range(0, numberOfClusters).mapToObj((i)->new DegreeBearingImpl(((double) i)*360./(double) numberOfClusters)));
        final Set<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> clusters = clusterer.getClusters();
        assert maneuvers.size() == clusters.stream().map((c)->c.size()).reduce((s1, s2)->s1+s2).get();
        final Set<Pair<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>, Double>> clustersAndLikelihoodOfBeingTackCluster =
                clusters.stream().map((c)->new Pair<>(c, getLikelihoodIsTackCluster(c, clusters))).collect(Collectors.toSet());
        // find most likely tack cluster, based on proximity of maneuver angles in cluster and opposite cluster as well as the speed ratio
        // between the cluster and its opposite jibe cluster candidate
        Pair<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>, Double> dominantClusterAndLikelihoodOfBeingTackCluster =
                clustersAndLikelihoodOfBeingTackCluster.stream().max(
                        (a, b)->(int) Math.signum(a.getB() - b.getB())).get();
        addWindFixes(dominantClusterAndLikelihoodOfBeingTackCluster.getA(), ManeuverType.TACK);
        Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> jibeCluster = getOppositeCluster(dominantClusterAndLikelihoodOfBeingTackCluster.getA(), clusters);
        if (jibeCluster != null) {
            addWindFixes(jibeCluster, ManeuverType.JIBE);
        }
        return new Triple<>(clusters, dominantClusterAndLikelihoodOfBeingTackCluster.getA(), jibeCluster);
    }

    private void addWindFixes(
            Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> dominantClusterAndLikelihoodOfBeingTackCluster,
            final ManeuverType maneuverType) {
        // TODO see bug 1562 comment #8: use WindWithConfidenceImpl internally and implement WindTrack.getAveragedWindWithConfidence
        for (ManeuverClassification mc : dominantClusterAndLikelihoodOfBeingTackCluster) {
            final SpeedWithBearingWithConfidence<Void> estimatedWindSpeedAndBearing = mc.getEstimatedWindSpeedAndBearing(maneuverType);
            if (estimatedWindSpeedAndBearing != null) {
                Wind windFromTack = new WindImpl(mc.getPosition(), mc.getTimePoint(), estimatedWindSpeedAndBearing.getObject());
                add(windFromTack);
            }
        }
    }
    
    /**
     * For the <code>tackClusterCandidate</code> add up the likelihoods for each maneuver contained that it is a tack.
     * If an opposite jibe cluster is found whose centroid middle COG is closer than
     * {@link #THRESHOLD_OPPOSITE_CLUSTER_DIFFERENCE_DEGREES} degrees to the expected value, its maneuvers are assessed
     * as jibes and their probabilities are added, making the <code>tackClusterCandidate</code> more likely as there are
     * more likely jibe candidates in the jibe cluster candidate.
     * <p>
     * 
     * If a jibe cluster candidate is found, the resulting number is scaled by the likelihood that the average speed
     * difference (weighted by the likelihood of being the expected maneuver type) between tack and jibe cluster makes
     * sense. Otherwise, the value is scaled down by a factor of .25 as a penalty for not having found a jibe cluster.
     * 
     * @return not a 0..1 likelihood but some scaled value that can grow beyond 1 and represents the sum for all
     *         relevant <code>tackClusterCandidate</code> maneuvers' likelihood that they are a tack, added to the
     *         likelihood sum for an opposite cluster that its maneuvers are jibes, scaled by the probability that the
     *         speed ratio between tack and jibe cluster makes sense (or .25 in case there is no jibe cluster candidate
     *         found within the threshold).
     */
    private double getLikelihoodIsTackCluster(Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> tackClusterCandidate,
            Set<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> clusters) {
        double tackClusterLikelihood = 0;
        for (ManeuverClassification mc : tackClusterCandidate) {
            tackClusterLikelihood += mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType.TACK).getA();
        }
        // TODO the cluster is more likely a tack cluster if two maneuver angle clusters are found that have small variance (no "into-the-wind" maneuvers other than tacks) 
        Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> jibeClusterCandidate = getOppositeCluster(tackClusterCandidate, clusters);
        final double speedMatchFactor;
        double jibeClusterLikelihood = 0;
        if (jibeClusterCandidate != null) {
            BoatClass boatClass = null; // TODO what if the race has more than one?
            for (ManeuverClassification mc : jibeClusterCandidate) {
                jibeClusterLikelihood += mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType.JIBE).getA();
                boatClass = mc.getCompetitor().getBoat().getBoatClass();
            }
            speedMatchFactor = getLikelihoodTackJibeSpeedRatio(tackClusterCandidate, jibeClusterCandidate, boatClass);
        } else {
            speedMatchFactor = .25; // no jibe cluster found; penalize for not being able to compare average speeds between tacks and jibes
        }
        return speedMatchFactor * tackClusterLikelihood * jibeClusterLikelihood;
    }

    private Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> getOppositeCluster(
            Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> cluster,
            Set<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> clusters) {
        final Bearing clusterCentroid = cluster.getCentroid();
        final Bearing opposite = clusterCentroid.reverse();
        Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> oppositeClusterCandidate =
                clusters.stream().min((a, b)->
                    (int) Math.signum(Math.abs(opposite.getDifferenceTo(a.getCentroid()).getDegrees()) -
                            Math.abs(opposite.getDifferenceTo(b.getCentroid()).getDegrees()))).get();
        final Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> result;
        if (Math.abs(opposite.getDifferenceTo(oppositeClusterCandidate.getCentroid()).getDegrees()) < THRESHOLD_OPPOSITE_CLUSTER_DIFFERENCE_DEGREES) {
            result = oppositeClusterCandidate;
        } else {
            result = null;
        }
        return result;
    }
    /**
     * By looking at the weighted speeds over ground averages, and based on the {@link #polarService} determines how
     * likely it is that the tack cluster actually contains tacks and the jibe cluster jibes. This is based on the idea
     * that if for the most likely wind speeds the jibes have a different (usually significantly higher) entry speed
     * than the tacks then this difference show show in the cluster elements. If they don't, then this should give a
     * penalty for the cluster likelihoods.
     * <p>
     * 
     * The weight of a maneuver for computing the weighted speed average is determined to be the
     * {@link ManeuverClassification#getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType) likelihood of the maneuver being
     * what it is assumed to be}.
     * @param boatClass 
     */
    private double getLikelihoodTackJibeSpeedRatio(
            Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> tackClusterCandidate,
            Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> jibeClusterCandidate, BoatClass boatClass) {
        Speed tackClusterWeightedAverageSpeed = getWeightedAverageSpeed(tackClusterCandidate, ManeuverType.TACK);
        Speed jibeClusterWeightedAverageSpeed = getWeightedAverageSpeed(jibeClusterCandidate, ManeuverType.JIBE);
        return polarService.getConfidenceForTackJibeSpeedRatio(tackClusterWeightedAverageSpeed,
                jibeClusterWeightedAverageSpeed, boatClass);
    }

    private Speed getWeightedAverageSpeed(Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> cluster, ManeuverType maneuverType) {
        double weightSum = 0;
        ScalableSpeed scalableSpeed = new ScalableSpeed(Speed.NULL);
        for (ManeuverClassification mc : cluster) {
            final Double weight = mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(maneuverType).getA();
            weightSum += weight;
            scalableSpeed = scalableSpeed.add(new ScalableSpeed(mc.getSpeedAtManeuverStart()).multiply(weight));
        }
        return scalableSpeed.divide(weightSum);
    }

    /**
     * For each maneuver from <code>maneuvers</code>, a {@link ManeuverClassification object} is created.
     */
    private Stream<ManeuverClassification> getManeuverClassifications(final Map<Maneuver, Competitor> maneuvers) {
        return maneuvers.entrySet().stream().map((maneuverAndCompetitor)->
            new ManeuverClassification(maneuverAndCompetitor.getValue(), maneuverAndCompetitor.getKey()));
    }

    private Map<Maneuver, Competitor> getAllManeuvers(boolean waitForLatest) {
        Map<Maneuver, Competitor> maneuvers = new HashMap<>();
        final Waypoint firstWaypoint = trackedRace.getRace().getCourse().getFirstWaypoint();
        final Waypoint lastWaypoint = trackedRace.getRace().getCourse().getLastWaypoint();
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            final TimePoint from = Util.getFirstNonNull(
                    firstWaypoint == null ? null : trackedRace.getMarkPassing(competitor, firstWaypoint) == null ? null : trackedRace.getMarkPassing(competitor, firstWaypoint).getTimePoint(),
                    trackedRace.getStartOfRace(), trackedRace.getStartOfTracking());
            final TimePoint to = Util.getFirstNonNull(
                    lastWaypoint == null ? null : trackedRace.getMarkPassing(competitor, lastWaypoint) == null ? null : trackedRace.getMarkPassing(competitor, lastWaypoint).getTimePoint(),
                    trackedRace.getEndOfRace(), trackedRace.getEndOfTracking(),
                    MillisecondsTimePoint.now());
            for (Maneuver maneuver : trackedRace.getManeuvers(competitor, from, to, waitForLatest)) {
                maneuvers.put(maneuver, competitor);
            }
        }
        return Collections.unmodifiableMap(maneuvers);
    }
    
    public Set<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> getClusters() {
        return clusters;
    }
    
    public Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> getTackCluster() {
        return tackCluster;
    }

    public Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> getJibeCluster() {
        return jibeCluster;
    }
    
    public String getStringRepresentation(boolean waitForLatest) {
        return getStringRepresentation(getClusters().stream(), waitForLatest);
    }
    
    public String getStringRepresentation(Stream<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> clusters, boolean waitForLatest) {
        StringBuilder stringRepresentation = new StringBuilder();
        stringRepresentation.delete(0, stringRepresentation.length());
        stringRepresentation.append(getManeuverClassificationColumnHeaders());
        stringRepresentation.append('\n');
        stringRepresentation.append(getManeuverClassificationColumnTypes());
        stringRepresentation.append('\n');
        stringRepresentation.append(getManeuverClassificationColumnHeaders());
        stringRepresentation.append('\n');
        final int[] id = new int[1];
        clusters.map((c)->c.stream()).reduce(Stream::concat).get().forEach((i)->{
            stringRepresentation.append(format(i, ""+id[0]++));
            stringRepresentation.append('\n');
        });
        return stringRepresentation.toString();
    }
}
