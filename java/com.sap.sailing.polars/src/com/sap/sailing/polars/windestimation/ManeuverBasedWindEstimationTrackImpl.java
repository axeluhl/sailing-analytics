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
import com.sap.sailing.domain.common.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.common.confidence.ConfidenceFactory;
import com.sap.sailing.domain.common.confidence.HasConfidence;
import com.sap.sailing.domain.common.confidence.impl.ScalableDouble;
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
    
    private static final Logger logger = Logger.getLogger(ManeuverBasedWindEstimationTrackImpl.class.getName());
    
    /**
     * Using a fairly low base confidence for this estimation wind track. It shall only serve as a
     * default and should be superseded by other wind sources easily.
     */
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;

    /**
     * We're trying to find the jibe cluster based on a candidate for the tack cluster. This constant tells how many degrees
     * the jibe cluster may be off from the reversed tack cluster's centroid in order to be accepted as jibe cluster candidate.
     */
    private static final double THRESHOLD_JIBE_CLUSTER_DIFFERENCE_DEGREES = 20;
    
    private final PolarDataService polarService;

    private final TrackedRace trackedRace;
    
    /**
     * Caches the {@link #getWeightedAverageMiddleManeuverCOGDeg(Cluster, ManeuverType)} results because these values are required several times to
     * determine the likelihood with which a cluster is a cluster of tacks or a cluster of jibes. Note that the un-weighted averages can simply be obtained
     * by asking the cluster for its {@link Cluster#getCentroid() centroid}.
     */
    private final Map<Pair<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>, ManeuverType>, Bearing>
        weightedAverageMiddleCOGForManeuverType;

    /**
     * Keeps the clusters that were used during the last {@link #analyzeRace(boolean)} run; this is for debugging, mostly,
     * and could be removed/commented again when this class works as expected.
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
    
    public ManeuverBasedWindEstimationTrackImpl(PolarDataService polarService, TrackedRace trackedRace, long millisecondsOverWhichToAverage, boolean waitForLatest)
            throws NotEnoughDataHasBeenAddedException {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */ false,
                /* nameForReadWriteLock */ ManeuverBasedWindEstimationTrackImpl.class.getName());
        this.polarService = polarService;
        this.trackedRace = trackedRace;
        this.weightedAverageMiddleCOGForManeuverType = new HashMap<>();
        long start = System.currentTimeMillis();
        Triple<Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>,
               List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>,
               List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>> clusterStructure =
               analyzeRace(waitForLatest);
        logger.fine("Computed virtual wind fixes from maneuvers for race "+trackedRace.getRace().getName()+" in "+(System.currentTimeMillis()-start)+"ms");
        clusters = clusterStructure.getA();
        tackClusters = clusterStructure.getB();
        jibeClusters = clusterStructure.getC();
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
        return "datapoint\tcompetitor\ttimePoint\tangleDeg\tboatSpeedKn\tcogDeg\tmiddleManeuverCourse\tlossM\ttackLikelihood\tjibeLikelihood";
    }

    public String getManeuverClassificationColumnTypes() {
        return "infoitem\tstring\tdate\tfloat\tfloat\tfloat\tfloat\tfloat\tfloat\tfloat";
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
    private Triple<Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>,
                   List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>,
                   List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>>> analyzeRace(
                           boolean waitForLatest) throws NotEnoughDataHasBeenAddedException {
        final Map<Maneuver, Competitor> maneuvers = getAllManeuvers(waitForLatest);
        // cluster into eight clusters by middle COG first, then aggregate tack likelihoods for each, and jibe likelihoods for opposite cluster
        final int numberOfClusters = 16;
        KMeansMappingClusterer<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> clusterer =
                new KMeansMappingClusterer<>(numberOfClusters,
                        getManeuverClassifications(maneuvers),
                        (mc)->new ScalableBearingAndScalableDouble(mc.getMiddleManeuverCourse(), mc.getManeuverAngleDeg()), // maps maneuver classification to cluster metric
                        // use an evenly distributed set of cluster seeds for clustering wind direction estimations
                        Stream.concat(IntStream.range(0, numberOfClusters/2).mapToObj((i)->
                            new Pair<>(new DegreeBearingImpl(((double) i)*360./(double) numberOfClusters/2), 45.)),
                            IntStream.range(0, numberOfClusters/2).mapToObj((i)->
                                new Pair<>(new DegreeBearingImpl(((double) i)*360./(double) numberOfClusters/2), -45.))));
        final Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters = clusterer.getClusters();
        assert maneuvers.size() == clusters.stream().map((c)->c.size()).reduce((s1, s2)->s1+s2).get();
        // Now work towards identifying the two tack clusters
        // The first two clusters in the resulting list are now our best bet for the tack clusters, assuming that the likelihood for the third element
        // in the list is already much lower. Should there be a significant degradation of likelihood between the first and the second cluster, we
        // will only use the first, particularly if the average middle COGs of the first two clusters differ significantly.
        final List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> tackClusters =
                clusters.stream()
                .sorted((c1, c2) -> (int) -Math.signum(getLikelihoodOfBeingTackCluster(c1, clusters)
                        - getLikelihoodOfBeingTackCluster(c2, clusters))).limit(2).collect(Collectors.toList());
        addWindFixes(tackClusters.stream(), ManeuverType.TACK);
        Bearing estimatedJibeMiddleCOG =
                tackClusters.isEmpty() ? null
                                       : (tackClusters.size() == 1 ? getWeightedAverageMiddleManeuverCOGDeg(tackClusters.get(0), ManeuverType.TACK)
                                                                  : new ScalableBearing(getWeightedAverageMiddleManeuverCOGDeg(tackClusters.get(0), ManeuverType.TACK)).add(
                                                                          new ScalableBearing(getWeightedAverageMiddleManeuverCOGDeg(tackClusters.get(1), ManeuverType.TACK))).
                                                                          divide(2)).reverse();
        final List<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> jibeClusters =
                getJibeClusters(estimatedJibeMiddleCOG, clusters).collect(Collectors.toList());
        addWindFixes(jibeClusters.stream(), ManeuverType.JIBE);
        return new Triple<>(clusters, tackClusters, jibeClusters);
    }

    private void addWindFixes(
            Stream<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters,
            final ManeuverType maneuverType) {
        // TODO see bug 1562 comment #8: use WindWithConfidenceImpl internally and implement WindTrack.getAveragedWindWithConfidence
        clusters.forEach((cluster) -> {
            for (ManeuverClassification mc : cluster) {
                final SpeedWithBearingWithConfidence<Void> estimatedWindSpeedAndBearing = mc.getEstimatedWindSpeedAndBearing(maneuverType);
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
     * turn based on what the {@link #polarService} thinks that this likelihood is and based on whether we find other
     * <code>clusters</code> that support this hypothesis. The likelihood is positively affected
     * 
     * <ul>
     * <li>if a second cluster with similar average middle COG is found that also has a high average likelihood of the
     * maneuvers contained being tacks</li>
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
     * {@link PolarDataService#getConfidenceForTackJibeSpeedRatio(Speed, Speed, BoatClass)}); background: for many
     * boat classes and many wind conditions, jibes have a significantly higher entry speed than tacks and can hence
     * be kept apart from the tacks even if they have similar maneuver angles.</li>
     * </ul>
     * 
     * @return a likelihood between 0..1 (inclusive)
     * 
     * @see ManeuverClassification#getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType)
     * @see PolarDataService#getManeuverLikelihoodAndTwsTwa(BoatClass, Speed, double, ManeuverType)
     */
    private double getLikelihoodOfBeingTackCluster(
            Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> cluster,
            Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters) {
        // start with the average of all of the cluster's maneuvers' likelihood to be a tack
        double averageTackLikelihood = getAverageLikelihoodOfBeingManeuver(ManeuverType.TACK, cluster.stream());
        Bearing approximateMiddleCOGForJibes = getWeightedAverageMiddleManeuverCOGDeg(cluster, ManeuverType.TACK).reverse();

        // TODO search for the other tack cluster
        // TODO search for head-up/bear-away cluster(s) to port and to starboard of cluster's weighted middle COG

        // under the assumption that cluster holds tacks, find the clusters that then most likely hold the jibes
        Stream<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> jibeClusters =
                getJibeClusters(approximateMiddleCOGForJibes, clusters);
        // TODO increase cluster's score if "good" jibe clusters are found; quality is defined by how well the angle matches and how well the speed ratio fits
        Speed tackClusterWeightedAverageSpeed = getWeightedAverageSpeed(cluster.stream(), ManeuverType.TACK);
        final double result;
        if (tackClusterWeightedAverageSpeed != null) {
            // find out how likely the speed ratio is between the candidate tack cluster and the corresponding
            // hypothetical jibe clusters
            double tackJibeLikelihoodBasedOnSpeedRatioAndAverageManeuverAngle = getLikelihoodOfBeingJibeCluster(
                    tackClusterWeightedAverageSpeed, jibeClusters.map((jc) -> jc.stream()).reduce(Stream::concat)
                            .orElse(Stream.empty()), getBoatClass());
            // Likely jibe clusters may raise the general likelihood by up to 20% of the value so far, but not to over 1.0
            result = Math.min(1.0, averageTackLikelihood * (1.0 + 0.2 * tackJibeLikelihoodBasedOnSpeedRatioAndAverageManeuverAngle));
        } else {
            result = .5; // no elements in the cluster, therefore no average speed; 50/50 chance that this would be a tack cluster
        }
        return result;
    }

    /**
     * For an empty stream, 0.0 is returned
     */
    private double getAverageLikelihoodOfBeingManeuver(ManeuverType maneuverType,
            Stream<ManeuverClassification> maneuverClassifications) {
        return maneuverClassifications
                .mapToDouble((mc)->mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(maneuverType).getA()).average().orElse(0.0);
    }
    
    private BoatClass getBoatClass() {
        return trackedRace.getRace().getBoatClass();
    }

    /**
     * Returns at most two clusters that hold maneuvers with the highest average probability of being a jibe and whose
     * {@link #getWeightedAverageMiddleManeuverCOGDeg(Cluster, ManeuverType) weighted average middle COG} fits that of
     * the expected value (<code>approximateMiddleCOGForJibes</code>) within
     * {@link #THRESHOLD_JIBE_CLUSTER_DIFFERENCE_DEGREES} degrees.
     */
    private Stream<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> getJibeClusters(
            final Bearing approximateMiddleCOGForJibes,
            Set<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>> clusters) {
        // TODO the result of getAverageLikelihoodOfBeingManeuver(ManeuverType.JIBE, a.stream()) should be cached
        return clusters.stream().sorted((a, b)->
                    (int) -Math.signum(getAverageLikelihoodOfBeingManeuver(ManeuverType.JIBE, a.stream()) -
                            getAverageLikelihoodOfBeingManeuver(ManeuverType.JIBE, b.stream()))).filter(
                                    (jibeClusterCandidate)->Math.abs(getWeightedAverageMiddleManeuverCOGDeg(jibeClusterCandidate, ManeuverType.JIBE).
                                                                getDifferenceTo(approximateMiddleCOGForJibes).getDegrees()) < THRESHOLD_JIBE_CLUSTER_DIFFERENCE_DEGREES).
                                                                limit(2);
    }
    
    /**
     * By looking at the weighted speeds over ground averages, and based on the {@link #polarService} determines how
     * likely it is that the jibe cluster contents are actually jibes. This is based on the idea that if for the most
     * likely wind speeds the jibes have a different (usually significantly higher) entry speed than the tacks then this
     * difference show show in the cluster elements. If they don't, then this should give a penalty for the cluster
     * likelihoods.
     * <p>
     * 
     * The weight of a maneuver for computing the weighted speed average is determined to be the
     * {@link ManeuverClassification#getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType) likelihood of the maneuver
     * being what it is assumed to be}.
     */
    private double getLikelihoodOfBeingJibeCluster(
            Speed tackClusterWeightedAverageSpeed,
            Stream<ManeuverClassification> jibeClustersContent,
            BoatClass boatClass) {
        final int[] count = new int[1];
        final double[] likelihoodSum = new double[1];
        Stream<ManeuverClassification> jibeClustersContentPeeker = jibeClustersContent.peek((mc)->{
            count[0]++;
            likelihoodSum[0] += mc.getLikelihoodAndTWSBasedOnSpeedAndAngle(ManeuverType.JIBE).getA();
        });
        Speed jibeClusterWeightedAverageSpeed = getWeightedAverageSpeed(jibeClustersContentPeeker, ManeuverType.JIBE);
        final double result;
        if (jibeClusterWeightedAverageSpeed != null) {
            double tackJibeSpeedRatioLikelihood = polarService.getConfidenceForTackJibeSpeedRatio(
                    tackClusterWeightedAverageSpeed, jibeClusterWeightedAverageSpeed, boatClass);
            if (count[0] > 0) {
                double averageJibeLikelihood = likelihoodSum[0] / count[0];
                result = averageJibeLikelihood * tackJibeSpeedRatioLikelihood;
            } else {
                throw new RuntimeException("Internal error: no maneuvers in jibe cluster candidate but still a valid weighted average speed "+jibeClusterWeightedAverageSpeed);
            }
        } else {
            result = 0.5; // no maneuvers in the jibe clusters; could be but may also not be a jibe cluster
        }
        return result;
    }

    private Speed getWeightedAverageSpeed(
            Stream<ManeuverClassification> cluster,
            ManeuverType maneuverType) {
        ConfidenceBasedAverager<Double, Speed, Void> averager = ConfidenceFactory.INSTANCE.createAverager(/* weigher */ null);
        HasConfidence<Double, Speed, Void> average = averager.getAverage(
                cluster.map(new ManeuverClassificationToHasConfidenceAndIsScalableAdapter<Double, Speed>(maneuverType,
                (mc)->new ScalableSpeed(mc.getSpeedAtManeuverStart()),
                polarService)).iterator(), /* at */ null);
        return average == null ? null : average.getObject();
    }

    /**
     * For each maneuver from <code>maneuvers</code>, a {@link ManeuverClassification object} is created.
     */
    Stream<ManeuverClassification> getManeuverClassifications(final Map<Maneuver, Competitor> maneuvers) {
        return maneuvers.entrySet().stream().map((maneuverAndCompetitor)->
            new ManeuverClassification(maneuverAndCompetitor.getValue(), maneuverAndCompetitor.getKey()));
    }

    /**
     * Package scope to let test fragment access it
     */
    Map<Maneuver, Competitor> getAllManeuvers(boolean waitForLatest) {
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
    
    /**
     * For a cluster of maneuver classifications, clustered two-dimensionally by middle COG and maneuver angle, computes the
     * weighted average of the middle COG where as the weight of each maneuver the likelihood that the maneuver was of type
     * <code>maneuverType</code> is used.<p>
     * 
     * The method has default package scope to allow tests in the same package in the test fragment to use it.
     * 
     * @return the weighted average middle COG
     */
    Bearing getWeightedAverageMiddleManeuverCOGDeg(
            Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble> cluster,
            ManeuverType maneuverType) {
        Pair<Cluster<ManeuverClassification, Pair<ScalableBearing, ScalableDouble>, Pair<Bearing, Double>, ScalableBearingAndScalableDouble>, ManeuverType> key = new Pair<>(
                cluster, maneuverType);
        Bearing result = weightedAverageMiddleCOGForManeuverType.get(key);
        if (result == null) {
            ConfidenceBasedAverager<DoublePair, Bearing, Void> averager = ConfidenceFactory.INSTANCE
                    .createAverager(/* weigher */null);
            HasConfidence<DoublePair, Bearing, Void> average = averager.getAverage(
                    cluster.stream()
                            .map(new ManeuverClassificationToHasConfidenceAndIsScalableAdapter<DoublePair, Bearing>(
                                    maneuverType, (mc) -> new ScalableBearing(mc.getMiddleManeuverCourse()),
                                    polarService)).iterator(), /* at */null);
            result = average.getObject();
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
        clusters.map((c)->c.stream()).reduce(Stream::concat).get().forEach((i)->{
            stringRepresentation.append(format(i, ""+id[0]++));
            stringRepresentation.append('\n');
        });
        return stringRepresentation.toString();
    }
}
