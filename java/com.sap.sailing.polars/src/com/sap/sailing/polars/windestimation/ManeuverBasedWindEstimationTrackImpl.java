package com.sap.sailing.polars.windestimation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoat;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CompetitorAndBoatImpl;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.DoublePair;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.common.confidence.ConfidenceFactory;
import com.sap.sailing.domain.common.confidence.HasConfidence;
import com.sap.sailing.domain.common.confidence.impl.ScalableDouble;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableSpeed;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.DegreeBearingImpl;
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

    private static final Logger logger = Logger.getLogger(ManeuverBasedWindEstimationTrackImpl.class.getName());

    /**
     * Using a fairly low base confidence for this estimation wind track. It shall only serve as a default and should be
     * superseded by other wind sources easily.
     */
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;

    /**
     * Trying to find the two tack clusters, for each cluster the opposite tack cluster is looked for. The likelihood
     * for another cluster to be a cluster's opposite tack cluster is determined by first computing the
     * {@link #getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(Cluster, double, Bearing)
     * angular distance}. For each so many degrees of distance as specified by this constant, the likelihood of the
     * cluster to be the opposite tack cluster is halved. This constant can also be used to assign likelihoods for other
     * cluster types based on their distance from an expected configuration of maneuver angle and middle COG.
     */
    private static final double ANGULAR_DISTANCE_FOR_HALF_CONFIDENCE_FOR_OPPOSITE_TACK_CLUSTER_DEG = 20.;

    /**
     * When one or two jibe clusters have been found whose
     * {@link #getLikelihoodOfBeingJibeCluster(Speed, Stream, BoatClass, Set) likelihood of being a jibe cluster} is
     * 100%, this is the boost factor by which the base likelihood for the tack cluster candidate will be increased (up
     * to a maximum of 1.0).
     */
    private static final double BOOST_FACTOR_FOR_FULL_JIBE_CLUSTER_LIKELIHOOD = 0.2;

    /**
     * When one or two head-up / bear-away clusters on the two tacks adjacent to the middle upwind COG have been found
     * whose {@link #getLikelihoodOfClusterBasedOnDistanceFromExpected(double, Bearing, Cluster) likelihood of being
     * such a cluster} is 100%, this is the boost factor by which the base likelihood for the tack cluster candidate
     * will be increased (up to a maximum of 1.0).
     */
    private static final double BOOST_FACTOR_FOR_FULL_HEAD_UP_BEAR_AWAY_CLUSTER_LIKELIHOOD = 0.1;

    /**
     * When looking for the head-up/bear-away clusters, which average maneuver angle should be expected? This is a bit
     * fuzzy because in some cases the head-up and bear-away maneuvers may be grouped into one cluster, resulting in an
     * average maneuver angle that is close to zero whereas in other cases they may form two clusters, one with positive
     * and the other with negative maneuver angles of somewhere between 10-20deg, typically.
     */
    private static final double EXPECTED_AVERAGE_HEAD_UP_AND_BEAR_AWAY_MANEUVER_ANGLE_DEG = 10.;

    /**
     * We're trying to find the jibe cluster based on a candidate for the tack cluster. This constant tells how many
     * degrees the jibe cluster may be off from the reversed tack cluster's centroid in order to be accepted as jibe
     * cluster candidate.
     */
    private static final double THRESHOLD_JIBE_CLUSTER_DIFFERENCE_DEGREES = 20;

    /**
     * For judging the likelihood of a cluster being a speed cluster, the speed ratio of the tack cluster candidate and
     * the jibe cluster candidate is computed and compared to what the polar service predicts. The basic likelihood of
     * the jibe cluster candidate (based on the maneuver angles relative to their speeds) is boosted by this factor for
     * a perfect speed ratio match.
     */
    private static final double BOOST_FACTOR_FOR_JIBE_TACK_SPEED_RATIO_LIKELIHOOD = 0;

    private final PolarDataService polarService;

    private final TrackedRace trackedRace;

    /**
     * Caches the {@link #getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(Cluster, ManeuverType)} results
     * because these values are required several times to determine the likelihood with which a cluster is a cluster of
     * tacks or a cluster of jibes. Note that the un-weighted averages can simply be obtained by asking the cluster for
     * its {@link Cluster#getCentroid() centroid}.
     */
    private final Map<Pair<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>, ManeuverType>, Pair<Bearing, Double>> weightedAverageMiddleCOGForManeuverType;

    /**
     * Keeps the clusters that were used during the last {@link #analyzeRace(boolean)} run; this is for debugging,
     * mostly, and could be removed/commented again when this class works as expected.
     */
    private final Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters;

    /**
     * References one element of {@link #clutsers} that was selected as the tack cluster; this is for debugging, mostly,
     * and could be removed/commented again when this class works as expected.
     */
    private final List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> tackClusters;

    /**
     * References one element of {@link #clusters} that was selected as the jibe cluster; this is for debugging, mostly,
     * and could be removed/commented again when this class works as expected.
     */
    private final List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> jibeClusters;

    public ManeuverBasedWindEstimationTrackImpl(PolarDataService polarService, TrackedRace trackedRace,
            long millisecondsOverWhichToAverage, boolean waitForLatest) throws NotEnoughDataHasBeenAddedException {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */false,
        /* nameForReadWriteLock */ManeuverBasedWindEstimationTrackImpl.class.getName());
        this.polarService = polarService;
        this.trackedRace = trackedRace;
        this.weightedAverageMiddleCOGForManeuverType = new HashMap<>();
        long start = System.currentTimeMillis();
        Triple<Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>, List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>, List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>> clusterStructure = analyzeRace(waitForLatest);
        logger.fine("Computed virtual wind fixes from maneuvers for race " + trackedRace.getRace().getName() + " in "
                + (System.currentTimeMillis() - start) + "ms");
        clusters = clusterStructure.getA();
        tackClusters = clusterStructure.getB();
        jibeClusters = clusterStructure.getC();
    }

    /**
     * Collects data about a maneuver that will be used to assign probabilities for maneuver types such as tack or jibe,
     * looking at a larger set of such objects and finding the most probable overall solution.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    public class ManeuverClassification {
        private final Competitor competitor;
        private final Boat boat;
        private final TimePoint timePoint;
        private final Position position;
        /**
         * Course change implied by the maneuver
         */
        private final double maneuverAngleDeg;
        private final SpeedWithBearing speedAtManeuverStart;
        private final Bearing middleManeuverCourse;
        private final Distance maneuverLossDistanceLost;
        private Pair<Double, SpeedWithBearingWithConfidence<Void>>[] likelihoodAndTWSBasedOnSpeedAndAngleCache;
        private ScalableBearingAndScalableDouble scalableMiddleManeuverCourseAndManeuverAngleDegCache;

        protected ManeuverClassification(CompetitorAndBoat competitorWithBoat, Maneuver maneuver) {
            super();
            this.competitor = competitorWithBoat.getCompetitor();
            this.boat = competitorWithBoat.getBoat();
            this.timePoint = maneuver.getTimePoint();
            this.position = maneuver.getPosition();
            this.maneuverAngleDeg = maneuver.getDirectionChangeInDegrees();
            this.speedAtManeuverStart = maneuver.getSpeedWithBearingBefore();
            this.middleManeuverCourse = maneuver.getSpeedWithBearingBefore().getBearing()
                    .middle(maneuver.getSpeedWithBearingAfter().getBearing());
            this.maneuverLossDistanceLost = maneuver.getManeuverLoss() == null ? null : maneuver.getManeuverLoss().getProjectedDistanceLost();
            @SuppressWarnings("unchecked")
            Pair<Double, SpeedWithBearingWithConfidence<Void>>[] myLikelihoodAndTWSBasedOnSpeedAndAngleCache = (Pair<Double, SpeedWithBearingWithConfidence<Void>>[]) new Pair<?, ?>[ManeuverType
                    .values().length];
            this.likelihoodAndTWSBasedOnSpeedAndAngleCache = myLikelihoodAndTWSBasedOnSpeedAndAngleCache;
        }

        public Competitor getCompetitor() {
            return competitor;
        }

        public Boat getBoat() {
            return boat;
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

        public ScalableBearingAndScalableDouble getScalableMiddleManeuverCourseAndManeuverAngleDeg() {
            if (scalableMiddleManeuverCourseAndManeuverAngleDegCache == null) {
                scalableMiddleManeuverCourseAndManeuverAngleDegCache = new ScalableBearingAndScalableDouble(
                        getMiddleManeuverCourse(), getManeuverAngleDeg());
            }
            return scalableMiddleManeuverCourseAndManeuverAngleDegCache;
        }

        public SpeedWithBearingWithConfidence<Void> getEstimatedWindSpeedAndBearing(ManeuverType maneuverType) {
            Pair<Double, SpeedWithBearingWithConfidence<Void>> likelihoodAndTWSBasedOnSpeedAndAngle = polarService
                    .getManeuverLikelihoodAndTwsTwa(getBoat().getBoatClass(),
                            getSpeedAtManeuverStart(), getManeuverAngleDeg(), maneuverType);
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

        public Distance getManeuverLossDistanceLost() {
            return maneuverLossDistanceLost;
        }

        /**
         * Computes the likelihood that the maneuver represented by this object is of the <code>type</code> requested.
         * The polar service may offer more than one possible wind condition for the given speed. In this case, the true
         * wind angle that fits the actual {@link #getManeuverAngleDeg() maneuver angle} better is chosen to judge how
         * close the maneuver is to the expected angle. The likelihood as well as the estimated true wind speed are
         * returned.
         * 
         * @return a value between 0 and 1
         */
        public Pair<Double, SpeedWithBearingWithConfidence<Void>> getLikelihoodAndTWSBasedOnSpeedAndAngle(
                final ManeuverType maneuverType) {
            if (likelihoodAndTWSBasedOnSpeedAndAngleCache[maneuverType.ordinal()] == null) {
                likelihoodAndTWSBasedOnSpeedAndAngleCache[maneuverType.ordinal()] = polarService
                        .getManeuverLikelihoodAndTwsTwa(getBoat().getBoatClass(),
                                getSpeedAtManeuverStart(), getManeuverAngleDeg(), maneuverType);
            }
            return likelihoodAndTWSBasedOnSpeedAndAngleCache[maneuverType.ordinal()];
        }

        @Override
        public String toString() {
            return format(this, /* id */null);
        }

    }

    public String getManeuverClassificationColumnHeaders() {
        return "datapoint\tcompetitor\ttimePoint\tangleDeg\tboatSpeedKn\tcogDeg\tmiddleManeuverCourse\tlossM\ttackLikelihood\tjibeLikelihood";
    }

    public String getManeuverClassificationColumnTypes() {
        return "infoitem\tstring\tdate\tfloat\tfloat\tfloat\tfloat\tfloat\tfloat\tfloat";
    }

    public String format(ManeuverClassification mc, String id) {
        DateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        final String prefix = mc.getCompetitor().getName() + "\t" + df.format(mc.getTimePoint().asDate()) + "\t"
                + mc.getManeuverAngleDeg() + "\t" + mc.getSpeedAtManeuverStart().getKnots() + "\t"
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
        result.append(mc.getManeuverLossDistanceLost() == null ? 0.0 : mc.getManeuverLossDistanceLost().getMeters());
        result.append("\t");
        result.append(mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType.TACK).getA());
        result.append("\t");
        result.append(mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType.JIBE).getA());
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
     * We evaluate each cluster's likelihood of being the tack cluster by computing the average across all its maneuvers
     * for that maneuver being a tack, solely based on its speed and maneuver angle, as
     * {@link PolarDataService#getManeuverLikelihoodAndTwsTwa(BoatClass, Speed, double, ManeuverType) judged by the
     * polar service}. If a cluster exists that has approximately the opposite average middle COG (the jibe cluster
     * candidate), the {@link #getLikelihoodOfBeingJibeCluster(Speed, Stream, BoatClass, Set) likelihood for its
     * maneuvers being jibes} are determined and may result in a "boost" for the basic probability of up to 20%.
     * Finally, the resulting likelihood is multiplied by how likely the speed ratio between the tack and jibe cluster
     * candidates is, based on the polar diagram. For this task, the cluster's maneuvers' average speed (weighted by
     * each maneuver's likelihood of being a tack) is divided by that of the jibe cluster's maneuvers' average speed
     * (weighted by each maneuver's likelihood of being a jibe) and assigned a likelihood by the polar service.
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
     * TODO with enough candidates at hand, apply temporal and spatial segmentation to account for heterogeneous wind
     * fields
     */
    private Triple<Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>, List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>, List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>> analyzeRace(
            boolean waitForLatest) throws NotEnoughDataHasBeenAddedException {
        final Map<Maneuver, CompetitorAndBoat> maneuvers = getAllManeuvers(waitForLatest);
        // cluster into eight clusters by middle COG first, then aggregate tack likelihoods for each, and jibe
        // likelihoods for opposite cluster
        final int numberOfClusters = 16;
        KMeansMappingClusterer<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> clusterer = new KMeansMappingClusterer<>(
                numberOfClusters, getManeuverClassifications(maneuvers),
                (mc) -> mc.getScalableMiddleManeuverCourseAndManeuverAngleDeg(), // maps maneuver classification to
                                                                                 // cluster metric
                // use an evenly distributed set of cluster seeds for clustering wind direction estimations
                Stream.concat(
                        IntStream.range(0, numberOfClusters / 2).mapToObj(
                                (i) -> new Pair<>(new DegreeBearingImpl(((double) i) * 360. / (double) numberOfClusters
                                        / 2), 45.)),
                        IntStream.range(0, numberOfClusters / 2).mapToObj(
                                (i) -> new Pair<>(new DegreeBearingImpl(((double) i) * 360. / (double) numberOfClusters
                                        / 2), -45.))));
        logger.fine("K-Means maneuver clusterer for wind estimation took " + clusterer.getNumberOfIterations()
                + " iterations");
        final Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters = clusterer
                .getClusters();
        assert maneuvers.size() == clusters.stream().map((c) -> c.size()).reduce((s1, s2) -> s1 + s2).get();
        // Now work towards identifying the two tack clusters
        final Map<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>, Double> likelihoodOfBeingTackCluster = new HashMap<>();
        for (Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> c : clusters) {
            likelihoodOfBeingTackCluster.put(c, getLikelihoodOfBeingTackCluster(c, clusters));
        }
        // The first two clusters in the resulting list are now our best bet for the tack clusters, assuming that the
        // likelihood for the third element
        // in the list is already much lower. Should there be a significant degradation of likelihood between the first
        // and the second cluster, we
        // will only use the first, particularly if the average middle COGs of the first two clusters differ
        // significantly.
        final List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> tackClusters = clusters
                .stream()
                .sorted((c1, c2) -> (int) -Math.signum(likelihoodOfBeingTackCluster.get(c1)
                        - likelihoodOfBeingTackCluster.get(c2))).limit(2).collect(Collectors.toList());
        addWindFixes(tackClusters.stream(), ManeuverType.TACK);
        Bearing estimatedJibeMiddleCOG = tackClusters.isEmpty() ? null
                : (tackClusters.size() == 1 ? getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(
                        tackClusters.get(0), ManeuverType.TACK).getA() : new ScalableBearing(
                        getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(tackClusters.get(0),
                                ManeuverType.TACK).getA()).add(
                        new ScalableBearing(getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(
                                tackClusters.get(1), ManeuverType.TACK).getA())).divide(2)).reverse();
        final List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> jibeClusters = getJibeClusters(
                estimatedJibeMiddleCOG, clusters).collect(Collectors.toList());
        addWindFixes(jibeClusters.stream(), ManeuverType.JIBE);
        return new Triple<>(clusters, tackClusters, jibeClusters);
    }

    private void addWindFixes(
            Stream<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters,
            final ManeuverType maneuverType) {
        // TODO see bug 1562 comment #8: use WindWithConfidenceImpl internally and implement
        // WindTrack.getAveragedWindWithConfidence
        clusters.forEach((cluster) -> {
            for (ManeuverClassification mc : cluster) {
                final SpeedWithBearingWithConfidence<Void> estimatedWindSpeedAndBearing = mc
                        .getEstimatedWindSpeedAndBearing(maneuverType);
                if (estimatedWindSpeedAndBearing != null) {
                    Wind windFromTack = new WindImpl(mc.getPosition(), mc.getTimePoint(), estimatedWindSpeedAndBearing
                            .getObject());
                    add(windFromTack);
                }
            }
        });
    }

    /**
     * Determines the likelihood of <code>cluster</code> being a cluster of tacks, based on the average likelihood of
     * the maneuver classifications in <code>cluster</code> to be maneuvers of type {@link ManeuverType#TACK TACK}, in
     * turn based on what the {@link #polarService} thinks that this
     * {@link PolarDataService#getManeuverLikelihoodAndTwsTwa(BoatClass, Speed, double, ManeuverType) likelihood} is and
     * based on whether we find other <code>clusters</code> that support this hypothesis. The likelihood is positively
     * affected
     * 
     * <ul>
     * <li>if a second cluster with similar average middle COG and a similar average maneuver angle with opposite sign
     * is found that also has a high average likelihood of its maneuvers being tacks</li>
     * <li>if other clusters are found that for the average true wind speed estimated from the maneuvers in
     * <code>cluster</code> seem like head-up/bear-away clusters on port/starboard tack with an average middle COG that
     * is half the tacking angle for that wind speed away from the tack cluster's average middle COG</li>
     * <li>if clusters with approximately reversed average middle COG are found, weighted by the maneuvers' likelihood
     * to be jibes</li>
     * <li>if the jibe cluster(s) are surrounded by head-up/bear-away clusters on port/starboard tack with their average
     * middle COG being approximately half the jibing angle at the average jibe maneuvers' wind speed away from the jibe
     * clusters' weighted average middle COG</li>
     * <li>the jibe clusters' weighted average speed relates to the tack clusters' weighted average speed in a way that
     * is supported by the {@link #polarService} (see
     * {@link PolarDataService#getConfidenceForTackJibeSpeedRatio(Speed, Speed, BoatClass)}); background: for many boat
     * classes and many wind conditions, jibes have a significantly higher entry speed than tacks and can hence be kept
     * apart from the tacks even if they have similar maneuver angles.</li>
     * </ul>
     * 
     * @return a likelihood between 0..1 (inclusive), obtained by multiplying the based likelihood of the
     *         <code>cluster</code> to hold tacks, based on the maneuver angles and their speed and how likely the
     *         {@link #polarService} considers this to be a tack; with boost factors added for the likelihood of having
     *         found a good jibe cluster and boost factors for the likelihoods of having found starboard and port tack
     *         clusters that hold the head-up/bear-away maneuvers. These boosts can add as much as
     *         {@link #BOOST_FACTOR_FOR_FULL_JIBE_CLUSTER_LIKELIHOOD} + 2*
     *         {@link #BOOST_FACTOR_FOR_FULL_HEAD_UP_BEAR_AWAY_CLUSTER_LIKELIHOOD} of the original value but will be
     *         capped at the maximum likelihood of 1.0.
     * 
     * @see ManeuverClassification#getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType)
     * @see PolarDataService#getManeuverLikelihoodAndTwsTwa(BoatClass, Speed, double, ManeuverType)
     */
    private double getLikelihoodOfBeingTackCluster(
            Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> cluster,
            Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters) {
        // start with the average of all of the cluster's maneuvers' likelihood to be a tack
        double averageTackLikelihood = getAverageLikelihoodOfBeingManeuver(ManeuverType.TACK, cluster.stream());
        final Bearing approximateMiddleCOG = getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(cluster,
                ManeuverType.TACK).getA();
        // search for the opposite tack cluster by finding the one that has the smallest angular distance to the
        // expected
        // middle COG (same as for the candidate cluster) and expected maneuver angle (the cluster's average maneuver
        // angle with inverted sign)
        final Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> oppositeTackCluster = clusters
                .stream()
                .filter((c) -> c != cluster)
                .min((a, b) -> (int) Math
                        .signum(getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(a,
                                -cluster.getCentroid().getB(), approximateMiddleCOG)
                                - getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(b,
                                        -cluster.getCentroid().getB(), approximateMiddleCOG))).orElse(null);
        // if the opposite tack cluster is empty, it's pretty unlikely that it's a tack cluster; assign a low default
        // probability of 10%
        final double likelihoodOfOppositeCluster = oppositeTackCluster.isEmpty() ? 0.1
                : getLikelihoodOfClusterBasedOnDistanceFromExpected(-cluster.getCentroid().getB(),
                        approximateMiddleCOG, oppositeTackCluster);
        // compute weighted averages across the current candidate cluster and its supposed opposite tack cluster where
        // the latter
        // is weighted by the likelihoodOfOppositeCluster whereas the candidate cluster is always weighed as 1.0 for
        // this purpose
        final double averageTackingAngleDeg = (Math.abs(getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(
                cluster, ManeuverType.TACK).getB()) + likelihoodOfOppositeCluster
                * Math.abs(getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(oppositeTackCluster,
                        ManeuverType.TACK).getB()))
                / (1 + likelihoodOfOppositeCluster);
        final Bearing averageUpwindCOG = new ScalableBearing(getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(
                cluster, ManeuverType.TACK).getA()).add(
                new ScalableBearing(getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(oppositeTackCluster,
                        ManeuverType.TACK).getA()).multiply(likelihoodOfOppositeCluster)).divide(
                1 + likelihoodOfOppositeCluster);

        // Now search for head-up/bear-away cluster(s) to port and to starboard of cluster's weighted middle COG
        final Bearing expectedUpwindStarboardTackCOG = averageUpwindCOG.add(new DegreeBearingImpl(
                -averageTackingAngleDeg / 2.));
        double starboardHeadUpBearAwayClusterLikelihood = getLikelihoodOfBestFittingHeadUpBearAwayCluster(clusters
                .stream().filter(c -> c != cluster), expectedUpwindStarboardTackCOG);
        final Bearing expectedUpwindPortTackCOG = averageUpwindCOG.add(new DegreeBearingImpl(
                averageTackingAngleDeg / 2.));
        double portHeadUpBearAwayClusterLikelihood = getLikelihoodOfBestFittingHeadUpBearAwayCluster(clusters.stream()
                .filter(c -> c != cluster), expectedUpwindPortTackCOG);

        // under the assumption that cluster holds tacks, find the clusters that then most likely hold the jibes
        final Bearing approximateMiddleCOGForJibes = averageUpwindCOG.reverse();
        Stream<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> jibeClusters = getJibeClusters(
                approximateMiddleCOGForJibes, clusters);
        // increase cluster's score if "good" jibe clusters are found; quality is defined by how well the angle matches
        // and how well the speed ratio fits
        Speed tackClusterWeightedAverageSpeed = getWeightedAverageSpeed(cluster.stream(), ManeuverType.TACK);
        final double result;
        if (tackClusterWeightedAverageSpeed != null) {
            // find out how likely the speed ratio is between the candidate tack cluster and the corresponding
            // hypothetical jibe clusters
            double jibeClusterLikelihood = getLikelihoodOfBeingJibeCluster(tackClusterWeightedAverageSpeed,
                    jibeClusters.map((jc) -> jc.stream()).reduce(Stream::concat).orElse(Stream.empty()),
                    getBoatClass(), clusters);
            // Likely jibe clusters may raise the general likelihood by up to 20% of the value so far, but not to over
            // 1.0
            result = Math
                    .min(1.0,
                            averageTackLikelihood
                                    * likelihoodOfOppositeCluster
                                    * (1.0 + BOOST_FACTOR_FOR_FULL_JIBE_CLUSTER_LIKELIHOOD * jibeClusterLikelihood
                                            + BOOST_FACTOR_FOR_FULL_HEAD_UP_BEAR_AWAY_CLUSTER_LIKELIHOOD
                                            * starboardHeadUpBearAwayClusterLikelihood + BOOST_FACTOR_FOR_FULL_HEAD_UP_BEAR_AWAY_CLUSTER_LIKELIHOOD
                                            * portHeadUpBearAwayClusterLikelihood));
        } else {
            result = .1; // no elements in the cluster, therefore no average speed; low propability that this would be a
                         // tack cluster
        }
        return result;
    }

    /**
     * Determines a likelihood for <code>cluster</code> being a head-up/bear-away cluster for the
     * <code>expectedMiddleCOG</code>.
     * 
     * @return a value between 0..1 (exclusive)
     * @see #getLikelihoodOfClusterBasedOnDistanceFromExpected(double, Bearing, Cluster)
     */
    private double getLikelihoodOfBestFittingHeadUpBearAwayCluster(
            Stream<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters,
            final Bearing expectedAverageMiddleCOG) {
        final Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> starboardTackHeadUpAndBearAwayCluster = clusters
                .max((a, b) -> (int) Math
                        .signum(getLikelihoodOfClusterBasedOnDistanceFromExpected(
                                EXPECTED_AVERAGE_HEAD_UP_AND_BEAR_AWAY_MANEUVER_ANGLE_DEG, expectedAverageMiddleCOG, a)
                                - getLikelihoodOfClusterBasedOnDistanceFromExpected(
                                        EXPECTED_AVERAGE_HEAD_UP_AND_BEAR_AWAY_MANEUVER_ANGLE_DEG,
                                        expectedAverageMiddleCOG, b))).orElse(null);
        double starboardHeadUpBearAwayClusterLikelihood = starboardTackHeadUpAndBearAwayCluster == null ? 0.0
                : getLikelihoodOfClusterBasedOnDistanceFromExpected(
                        EXPECTED_AVERAGE_HEAD_UP_AND_BEAR_AWAY_MANEUVER_ANGLE_DEG, expectedAverageMiddleCOG,
                        starboardTackHeadUpAndBearAwayCluster);
        return starboardHeadUpBearAwayClusterLikelihood;
    }

    /**
     * Assigns a likelihood to a cluster based on its
     * {@link #getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(Cluster, double, Bearing)
     * "angular distance"} from an expected average middle COG and Tells how likely it is that the two clusters are
     * opposite tack clusters. This is determined by looking at the
     * {@link #getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(Cluster, double, Bearing)
     * angular distance} between the two clusters regarding their middle COG and their average maneuver angles. The
     * likelihood decreases exponentially with increasing angle differences. It is 1.0 for an exact match and reaches
     * 0.5 for an angular distance of 20deg.
     */
    private double getLikelihoodOfClusterBasedOnDistanceFromExpected(
            double expectedManeuverAngleDeg,
            Bearing expectedApproximateMiddleCOG,
            Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> oppositeTackCluster) {
        final double clusterDistanceFromExpected = getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(
                oppositeTackCluster, expectedManeuverAngleDeg, expectedApproximateMiddleCOG);
        return getLikelihoodOfClusterBasedOnDistanceFromExpected(clusterDistanceFromExpected);
    }

    /**
     * Assigns a likelihood to a cluster based on its
     * {@link #getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(Cluster, double, Bearing)
     * "angular distance"} from an expected average middle COG and Tells how likely it is that the two clusters are
     * opposite tack clusters. This is determined by looking at the
     * {@link #getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(Cluster, double, Bearing)
     * angular distance} between the two clusters regarding their middle COG and their average maneuver angles. The
     * likelihood decreases exponentially with increasing angle differences. It is 1.0 for an exact match and reaches
     * 0.5 for an angular distance of 20deg (see
     * {@link #ANGULAR_DISTANCE_FOR_HALF_CONFIDENCE_FOR_OPPOSITE_TACK_CLUSTER_DEG}).
     * 
     * @param clusterDistanceFromExpected
     *            an angular distance as computed by {@link #getClusterDistance(double, Bearing, double, Bearing)} and
     *            {@link #getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(Cluster, double, Bearing)}
     *            .
     */
    private double getLikelihoodOfClusterBasedOnDistanceFromExpected(final double clusterDistanceFromExpected) {
        return Math.exp(Math.log(0.5)
                * (clusterDistanceFromExpected / ANGULAR_DISTANCE_FOR_HALF_CONFIDENCE_FOR_OPPOSITE_TACK_CLUSTER_DEG));
    }

    /**
     * A euklidian degree distance based on the two degree distances between the maneuver angles and the middle COGs.
     * The expected pair is passed as <code>expcetedManeuverAngleDeg</code> and
     * <code>expectedApproximateMiddleCOG</code> whereas the actuals are determined by computing the
     * {@link ManeuverType#TACK}-weighted average for those values from <code>cluster</code>.
     */
    private double getClusterDistanceBasedOnAverageManeuverLikelihoodAndWeightedAverageMiddleCOG(
            Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> cluster,
            double expectedManeuverAngleDeg, Bearing expectedApproximateMiddleCOG) {
        final Pair<Bearing, Double> weightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg = getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(
                cluster, ManeuverType.TACK);
        final Bearing weightedAverageMiddleCOG = weightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg.getA();
        final Double weightedAverageManeuverAngle = weightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg.getB();
        return getClusterDistance(expectedManeuverAngleDeg, expectedApproximateMiddleCOG, weightedAverageManeuverAngle,
                weightedAverageMiddleCOG);
    }

    /**
     * A euklidian degree distance based on the two degree distances between the maneuver angles and the middle COGs.
     */
    private double getClusterDistance(double expectedManeuverAngleDeg, Bearing expectedApproximateMiddleCOG,
            final double weightedAverageManeuverAngle, final Bearing weightedAverageMiddleCOG) {
        double middleCOGDiff = weightedAverageMiddleCOG.getDifferenceTo(expectedApproximateMiddleCOG).getDegrees();
        double maneuverAngleDiff = weightedAverageManeuverAngle - expectedManeuverAngleDeg;
        return Math.sqrt(middleCOGDiff * middleCOGDiff + maneuverAngleDiff * maneuverAngleDiff);
    }

    /**
     * For an empty stream, 0.0 is returned
     */
    private double getAverageLikelihoodOfBeingManeuver(ManeuverType maneuverType,
            Stream<ManeuverClassification> maneuverClassifications) {
        return maneuverClassifications
                .mapToDouble((mc) -> mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(maneuverType).getA()).average()
                .orElse(0.0);
    }

    private BoatClass getBoatClass() {
        return trackedRace.getRace().getBoatClass();
    }

    /**
     * Returns at most two clusters that hold maneuvers with the highest average probability of being a jibe and whose
     * {@link #getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(Cluster, ManeuverType) weighted average middle
     * COG} fits that of the expected value (<code>approximateMiddleCOGForJibes</code>) within
     * {@link #THRESHOLD_JIBE_CLUSTER_DIFFERENCE_DEGREES} degrees.
     */
    private Stream<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> getJibeClusters(
            final Bearing approximateMiddleCOGForJibes,
            Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters) {
        // TODO the result of getAverageLikelihoodOfBeingManeuver(ManeuverType.JIBE, a.stream()) should be cached
        return clusters
                .stream()
                .sorted((a, b) -> (int) -Math.signum(getAverageLikelihoodOfBeingManeuver(ManeuverType.JIBE, a.stream())
                        - getAverageLikelihoodOfBeingManeuver(ManeuverType.JIBE, b.stream())))
                .filter((jibeClusterCandidate) -> Math.abs(getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(
                        jibeClusterCandidate, ManeuverType.JIBE).getA().getDifferenceTo(approximateMiddleCOGForJibes)
                        .getDegrees()) < THRESHOLD_JIBE_CLUSTER_DIFFERENCE_DEGREES).limit(2);
    }

    /**
     * By looking at the weighted speeds over ground averages, and based on the {@link #polarService} determines how
     * {@link PolarDataService#getManeuverLikelihoodAndTwsTwa(BoatClass, Speed, double, ManeuverType) likely} it is that
     * the jibe cluster contents are actually jibes. This is based on the idea that if for the most likely wind speeds
     * the jibes have a different (usually significantly higher) entry speed than the tacks then this difference should
     * show in the cluster elements. If they don't, then this should give a penalty for the cluster likelihoods.
     * <p>
     * 
     * The weight of a maneuver for computing the weighted speed average is determined to be the
     * {@link ManeuverClassification#getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType) likelihood of the maneuver
     * being what it is assumed to be}.
     */
    private double getLikelihoodOfBeingJibeCluster(
            Speed tackClusterWeightedAverageSpeed,
            Stream<ManeuverClassification> jibeClustersContent,
            BoatClass boatClass,
            Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters) {
        final int[] count = new int[1];
        final double[] likelihoodSum = new double[1];
        final ScalableBearing[] scaledAverageDownwindCOG = new ScalableBearing[1];
        final double[] scaledAbsJibingAngleSum = new double[1];
        Stream<ManeuverClassification> jibeClustersContentPeeker = jibeClustersContent.peek((mc) -> {
            count[0]++;
            final double likelihood = mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType.JIBE).getA();
            likelihoodSum[0] += likelihood;
            final ScalableBearing scaledCOG = new ScalableBearing(mc.getMiddleManeuverCourse()).multiply(likelihood);
            if (scaledAverageDownwindCOG[0] == null) {
                scaledAverageDownwindCOG[0] = scaledCOG;
            } else {
                scaledAverageDownwindCOG[0] = scaledAverageDownwindCOG[0].add(scaledCOG);
            }
            scaledAbsJibingAngleSum[0] += likelihood * Math.abs(mc.getManeuverAngleDeg());
        });
        Speed jibeClusterWeightedAverageSpeed = getWeightedAverageSpeed(jibeClustersContentPeeker, ManeuverType.JIBE);
        Bearing averageDownwindCOG = scaledAverageDownwindCOG[0] == null ? null : scaledAverageDownwindCOG[0]
                .divide(likelihoodSum[0]);
        double absWeightedAverageJibingAbgle = scaledAbsJibingAngleSum[0] / likelihoodSum[0];
        final double result;
        if (jibeClusterWeightedAverageSpeed != null) {
            double tackJibeSpeedRatioLikelihood = polarService.getConfidenceForTackJibeSpeedRatio(
                    tackClusterWeightedAverageSpeed, jibeClusterWeightedAverageSpeed, boatClass);
            if (count[0] > 0) {
                double averageJibeLikelihood = likelihoodSum[0] / count[0];
                // Now search for head-up/bear-away cluster(s) to port and to starboard of cluster's weighted middle COG
                final Bearing expectedDownwindStarboardTackCOG = averageDownwindCOG.add(new DegreeBearingImpl(
                        +absWeightedAverageJibingAbgle / 2.));
                double starboardHeadUpBearAwayClusterLikelihood = getLikelihoodOfBestFittingHeadUpBearAwayCluster(
                        clusters.stream(), expectedDownwindStarboardTackCOG);
                final Bearing expectedDownwindPortTackCOG = averageDownwindCOG.add(new DegreeBearingImpl(
                        -absWeightedAverageJibingAbgle / 2.));
                double portHeadUpBearAwayClusterLikelihood = getLikelihoodOfBestFittingHeadUpBearAwayCluster(
                        clusters.stream(), expectedDownwindPortTackCOG);
                result = Math
                        .min(1.0,
                                averageJibeLikelihood
                                        * (1.0 + BOOST_FACTOR_FOR_JIBE_TACK_SPEED_RATIO_LIKELIHOOD
                                                * tackJibeSpeedRatioLikelihood
                                                + BOOST_FACTOR_FOR_FULL_HEAD_UP_BEAR_AWAY_CLUSTER_LIKELIHOOD
                                                * starboardHeadUpBearAwayClusterLikelihood + BOOST_FACTOR_FOR_FULL_HEAD_UP_BEAR_AWAY_CLUSTER_LIKELIHOOD
                                                * portHeadUpBearAwayClusterLikelihood));
            } else {
                throw new RuntimeException(
                        "Internal error: no maneuvers in jibe cluster candidate but still a valid weighted average speed "
                                + jibeClusterWeightedAverageSpeed);
            }
        } else {
            result = 0.1; // no maneuvers in the jibe clusters; could be but may also not be a jibe cluster
        }
        return result;
    }

    private Speed getWeightedAverageSpeed(Stream<ManeuverClassification> cluster, ManeuverType maneuverType) {
        ConfidenceBasedAverager<Double, Speed, Void> averager = ConfidenceFactory.INSTANCE
                .createAverager(/* weigher */null);
        HasConfidence<Double, Speed, Void> average = averager.getAverage(
                cluster.map(
                        new ManeuverClassificationToHasConfidenceAndIsScalableAdapter<Double, Speed>(maneuverType,
                                (mc) -> new ScalableSpeed(mc.getSpeedAtManeuverStart()), polarService)).iterator(), /* at */
                null);
        return average == null ? null : average.getObject();
    }

    /**
     * For each maneuver from <code>maneuvers</code>, a {@link ManeuverClassification object} is created.
     */
    Stream<ManeuverClassification> getManeuverClassifications(final Map<Maneuver, CompetitorAndBoat> maneuvers) {
        return maneuvers
                .entrySet()
                .stream()
                .map((maneuverAndCompetitor) -> new ManeuverClassification(maneuverAndCompetitor.getValue(),
                        maneuverAndCompetitor.getKey()));
    }

    /**
     * Package scope to let test fragment access it
     */
    Map<Maneuver, CompetitorAndBoat> getAllManeuvers(boolean waitForLatest) {
        Map<Maneuver, CompetitorAndBoat> maneuvers = new HashMap<>();
        final Waypoint firstWaypoint = trackedRace.getRace().getCourse().getFirstWaypoint();
        final Waypoint lastWaypoint = trackedRace.getRace().getCourse().getLastWaypoint();
        for (Map.Entry<Competitor, Boat> competitorAndBoat : trackedRace.getRace().getCompetitorsAndTheirBoats().entrySet()) {
            Competitor competitor = competitorAndBoat.getKey();
            Boat boat = competitorAndBoat.getValue();
            final TimePoint from = Util.getFirstNonNull(
                    firstWaypoint == null ? null : trackedRace.getMarkPassing(competitor, firstWaypoint) == null ? null
                            : trackedRace.getMarkPassing(competitor, firstWaypoint).getTimePoint(), trackedRace
                            .getStartOfRace(), trackedRace.getStartOfTracking());
            final TimePoint to = Util.getFirstNonNull(
                    lastWaypoint == null ? null : trackedRace.getMarkPassing(competitor, lastWaypoint) == null ? null
                            : trackedRace.getMarkPassing(competitor, lastWaypoint).getTimePoint(), trackedRace
                            .getEndOfRace(), trackedRace.getEndOfTracking(), MillisecondsTimePoint.now());
            for (Maneuver maneuver : trackedRace.getManeuvers(competitor, from, to, waitForLatest)) {
                maneuvers.put(maneuver, new CompetitorAndBoatImpl(competitor, boat));
            }
        }
        return Collections.unmodifiableMap(maneuvers);
    }

    /**
     * For a cluster of maneuver classifications, clustered two-dimensionally by middle COG and maneuver angle, computes
     * the weighted average of the middle COG and maneuver angle where as the weight of each maneuver the likelihood
     * that the maneuver was of type <code>maneuverType</code> is used.
     * <p>
     * 
     * The method has default package scope to allow tests in the same package in the test fragment to use it.
     * 
     * @return the weighted average middle COG
     */
    Pair<Bearing, Double> getWeightedAverageMiddleManeuverCOGDegAndManeuverAngleDeg(
            Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> cluster,
            ManeuverType maneuverType) {
        Pair<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>, ManeuverType> key = new Pair<>(
                cluster, maneuverType);
        Pair<Bearing, Double> result = weightedAverageMiddleCOGForManeuverType.get(key);
        if (result == null) {
            ConfidenceBasedAverager<DoublePair, Bearing, Void> middleCOGAverager = ConfidenceFactory.INSTANCE
                    .createAverager(/* weigher */null);
            HasConfidence<DoublePair, Bearing, Void> average = middleCOGAverager.getAverage(
                    cluster.stream()
                            .map(new ManeuverClassificationToHasConfidenceAndIsScalableAdapter<DoublePair, Bearing>(
                                    maneuverType, (mc) -> new ScalableBearing(mc.getMiddleManeuverCourse()),
                                    polarService)).iterator(), /* at */null);
            ConfidenceBasedAverager<Double, Double, Void> maneuverAngleAverager = ConfidenceFactory.INSTANCE
                    .createAverager(/* weigher */null);
            HasConfidence<Double, Double, Void> maneuverAngleAverageDeg = maneuverAngleAverager.getAverage(
                    cluster.stream()
                            .map(new ManeuverClassificationToHasConfidenceAndIsScalableAdapter<Double, Double>(
                                    maneuverType, (mc) -> new ScalableDouble(mc.getManeuverAngleDeg()), polarService))
                            .iterator(), /* at */null);
            result = new Pair<>(average.getObject(), maneuverAngleAverageDeg.getObject());
            weightedAverageMiddleCOGForManeuverType.put(key, result);
        }
        return result;
    }

    public Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> getClusters() {
        return clusters;
    }

    public List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> getTackClusters() {
        return tackClusters;
    }

    public List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> getJibeClusters() {
        return jibeClusters;
    }

    public String getStringRepresentation(boolean waitForLatest) {
        return getStringRepresentation(getClusters().stream(), waitForLatest);
    }

    public String getStringRepresentation(
            Stream<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters,
            boolean waitForLatest) {
        StringBuilder stringRepresentation = new StringBuilder();
        stringRepresentation.delete(0, stringRepresentation.length());
        stringRepresentation.append(getManeuverClassificationColumnHeaders());
        stringRepresentation.append('\n');
        stringRepresentation.append(getManeuverClassificationColumnTypes());
        stringRepresentation.append('\n');
        stringRepresentation.append(getManeuverClassificationColumnHeaders());
        stringRepresentation.append('\n');
        final int[] id = new int[1];
        clusters.map((c) -> c.stream()).reduce(Stream::concat).get().forEach((i) -> {
            stringRepresentation.append(format(i, "" + id[0]++));
            stringRepresentation.append('\n');
        });
        return stringRepresentation.toString();
    }
}
