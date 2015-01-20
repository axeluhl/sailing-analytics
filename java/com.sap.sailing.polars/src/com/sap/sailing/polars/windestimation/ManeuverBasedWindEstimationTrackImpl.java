package com.sap.sailing.polars.windestimation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.DoublePair;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.common.confidence.ConfidenceFactory;
import com.sap.sailing.domain.common.confidence.HasConfidence;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.util.kmeans.Cluster;
import com.sap.sailing.util.kmeans.KMeansMappingClusterer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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
     * Maneuvers are only considered if their actual course change differs less than these many degrees from
     * the prediction made by the polar diagram (VPP).
     */
    private static final double MANEUVER_ANGLE_DEVIATION_IN_DEGREES_THRESHOLD = 20;

    /**
     * Jibes are sailed on downwind legs which on average have a higher speed than upwind beats. The algorithm can use
     * this in case of ambiguous maneuver angles to distinguish jibes from tacks, but only if the two speeds are
     * sufficiently different. This constant defines what "sufficiently different" means. The constant tells the factor
     * by which the average speed into the jibe must be greater than the average speed into a tack so that the speed
     * will be used as a classifcation criterion.
     */
    private static final double RATIO_JIBE_VERSUS_TACK_START_SPEED_THRESHOLD = 1.2;
    
    private final PolarDataService polarService;

    private final TrackedRace trackedRace;
    
    private final StringBuilder stringRepresentation;

    public ManeuverBasedWindEstimationTrackImpl(PolarDataService polarService, TrackedRace trackedRace, long millisecondsOverWhichToAverage)
            throws NotEnoughDataHasBeenAddedException {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */ false,
                /* nameForReadWriteLock */ ManeuverBasedWindEstimationTrackImpl.class.getName());
        this.polarService = polarService;
        this.trackedRace = trackedRace;
        this.stringRepresentation = new StringBuilder();
        analyzeRace();
    }

    /**
     * Collects data about a maneuver that will be used to assign probabilities for maneuver types such as tack or
     * jibe, looking at a larger set of such objects and finding the most probable overall solution.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private static class ManeuverClassification {
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
        private final LegType legType;
        private final Tack tack;
        private final SpeedWithBearingWithConfidence<Void> estimatedTrueWindSpeedAndAngle;
        
        /**
         * Each maneuver classification will usually have an "opposite" that has {@link #legType} {@link LegType#UPWIND} when
         * this one has {@link #legType} {@link LegType#DOWNWIND} and vice versa. Since this is a cyclic relationship it is not
         * set up during object construction, but a {@link #setOpposite(ManeuverClassification) setter} is offered for it.
         */
        private ManeuverClassification opposite;
        
        protected ManeuverClassification(Competitor competitor, Maneuver maneuver, LegType legType, Tack tack,
                SpeedWithBearingWithConfidence<Void> estimatedTrueWindSpeedAndAngle) {
            super();
            this.competitor = competitor;
            this.timePoint = maneuver.getTimePoint();
            this.position = maneuver.getPosition();
            this.legType = legType;
            this.tack = tack;
            this.estimatedTrueWindSpeedAndAngle = estimatedTrueWindSpeedAndAngle;
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

        public Bearing getEstimatedWindBearing() {
            switch (getLegType()) {
            case UPWIND:
                return getMiddleManeuverCourse().reverse();
            case DOWNWIND:
                return getMiddleManeuverCourse();
            default:
                throw new IllegalStateException("Found leg type "+getLegType()+" but can only handle "+LegType.UPWIND.name()+
                        " and "+LegType.DOWNWIND.name());
            }
        }
        
        public Distance getManeuverLoss() {
            return maneuverLoss;
        }

        public LegType getLegType() {
            return legType;
        }

        public Tack getTack() {
            return tack;
        }

        public SpeedWithBearingWithConfidence<Void> getEstimatedTrueWindSpeedAndAngle() {
            return estimatedTrueWindSpeedAndAngle;
        }
        
        public double getAbsoluteOffsetBetweenActualAndExpectedManeuverAngle() {
            return Math.abs(getEstimatedTrueWindSpeedAndAngle().getObject().getBearing().getDegrees()*2)-Math.abs(getManeuverAngleDeg());
        }
        
        public static String getToStringColumnHeaders() {
            return "datapoint\tcompetitor\ttimePoint\tangleDeg\tboatSpeedKn\tcogDeg\tmiddleManeuverCourse\twindEstimationFromDeg\tlossM\tassumedLegType\tassumedTack\testimatedTrueWindSpeedKn\testimatedTrueWindAngleDeg\toffsetFromExpectedManeuverAngle";
        }
        
        public static String getToStringColumnTypes() {
            return "infoitem\tstring\tdate\tfloat\tfloat\tfloat\tfloat\tfloat\tfloat\tstring\tstring\tfloat\tfloat\tfloat";
        }
        
        @Override
        public String toString() {
            return toString(/* id */ null);
        }
        
        public String toString(String id) {
            DateFormat df = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
            final String prefix = getCompetitor().getName() + "\t" + df.format(getTimePoint().asDate()) + "\t" + getManeuverAngleDeg()
                    + "\t" + getSpeedAtManeuverStart().getKnots() + "\t"
                    + getSpeedAtManeuverStart().getBearing().getDegrees();
            final StringBuilder result = new StringBuilder();
            if (id != null) {
                result.append("ID");
                result.append(id);
                result.append('\t');
            }
            result.append(prefix);
            result.append("\t");
            result.append(getMiddleManeuverCourse().getDegrees());
            result.append("\t");
            result.append(getEstimatedWindBearing().reverse().getDegrees());
            result.append("\t");
            result.append(getManeuverLoss() == null ? 0.0 : getManeuverLoss().getMeters());
            result.append("\t");
            result.append(getLegType());
            result.append("\t");
            result.append(getTack());
            result.append("\t");
            result.append(getEstimatedTrueWindSpeedAndAngle().getObject().getKnots());
            result.append("\t");
            result.append(getEstimatedTrueWindSpeedAndAngle().getObject().getBearing().getDegrees());
            result.append("\t");
            result.append(Math.abs(Math.abs(getEstimatedTrueWindSpeedAndAngle().getObject().getBearing().getDegrees()*2)-Math.abs(getManeuverAngleDeg())));
            return result.toString();
        }

        public void setOpposite(ManeuverClassification maneuverClassification) {
            this.opposite = maneuverClassification;
        }

        public ManeuverClassification getOpposite() {
            return opposite;
        }
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
     * We evaluate each clusters likelihood of being the tack cluster by for each of its maneuvers adding up the
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
    private void analyzeRace() throws NotEnoughDataHasBeenAddedException {
        final Map<Maneuver, Competitor> maneuvers = getAllManeuvers();
        Set<ManeuverClassification> maneuverClassifications = getManeuverClassifications(maneuvers);
        // Now cluster the maneuvers by estimated maneuver angle
        // constrain the clustering to those maneuvers that are sufficiently close to the expected maneuver angle
        // TODO cluster into eight clusters by middle COG first, then aggregate tack likelihoods for each, and jibe likelihoods for opposite cluster
        // TODO compare weighted speed average for tack and jibe cluster candidates and let polar service assign probability for the ratio
        // TODO determine confidence of tack cluster being the tack cluster by comparing ratings against other cluster pairs
        // TODO create wind fixes from tack and jibe cluster maneuvers (optionally with a threshold for minimum confidence of the maneuver being what it was guessed to be)
        KMeansMappingClusterer<ManeuverClassification, DoublePair, Bearing, ScalableBearing> clusterer =
                new KMeansMappingClusterer<>(4,
                        maneuverClassifications.stream().filter(
                                (mc)->mc.getAbsoluteOffsetBetweenActualAndExpectedManeuverAngle()<MANEUVER_ANGLE_DEVIATION_IN_DEGREES_THRESHOLD),
                        (mc)->new ScalableBearing(mc.getEstimatedWindBearing()), // maps maneuver classification to cluster metric
                        // use an evenly distributed set of cluster seeds for clustering wind direction estimations
                        Arrays.<Bearing>asList(new DegreeBearingImpl(0), new DegreeBearingImpl(90), new DegreeBearingImpl(180), new DegreeBearingImpl(270)).stream());
        final Set<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> clusters = clusterer.getClusters();
        Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> dominantCluster = clusters.stream().max((a, b)->a.size()-b.size()).get();
        Pair<Set<SpeedWithConfidence<Void>>, Set<SpeedWithConfidence<Void>>> averageSpeedsIntoTacksAndJibes = dominantCluster.stream().collect(
                ()->new Pair<Set<SpeedWithConfidence<Void>>, Set<SpeedWithConfidence<Void>>>(new HashSet<>(), new HashSet<>()),
                (resultSoFar, mc)->{
                    if (mc.getLegType() == LegType.UPWIND) {
                        resultSoFar.getA().add(new SpeedWithConfidenceImpl<Void>(mc.getSpeedAtManeuverStart(),
                                /* high confidence for small deviation from expected maneuver angle */
                                Math.max(0, (MANEUVER_ANGLE_DEVIATION_IN_DEGREES_THRESHOLD-mc.getAbsoluteOffsetBetweenActualAndExpectedManeuverAngle()))/
                                        MANEUVER_ANGLE_DEVIATION_IN_DEGREES_THRESHOLD, /* relative to */ null));
                    } else { // DOWNWIND
                        resultSoFar.getB().add(new SpeedWithConfidenceImpl<Void>(mc.getSpeedAtManeuverStart(),
                                /* high confidence for small deviation from expected maneuver angle */
                                Math.max(0, (MANEUVER_ANGLE_DEVIATION_IN_DEGREES_THRESHOLD-mc.getAbsoluteOffsetBetweenActualAndExpectedManeuverAngle()))/
                                        MANEUVER_ANGLE_DEVIATION_IN_DEGREES_THRESHOLD, /* relative to */ null));
                    }
                },
                (s1, s2)->{ s1.getA().addAll(s2.getA()); s1.getB().addAll(s2.getB()); });
        final ConfidenceBasedAverager<Double, Speed, Void> speedAverager = ConfidenceFactory.INSTANCE.createAverager(/* weigher */ null);
        HasConfidence<Double, Speed, Void> averageSpeedIntoTack = speedAverager.getAverage(averageSpeedsIntoTacksAndJibes.getA(), /* at */ null);
        HasConfidence<Double, Speed, Void> averageSpeedIntoJibe = speedAverager.getAverage(averageSpeedsIntoTacksAndJibes.getB(), /* at */ null);
        // it is possible that the speed differences are miniscule, not sufficient to decide with certainty what was a tack and what a jibe
        if (averageSpeedIntoJibe.getObject().getKnots() > RATIO_JIBE_VERSUS_TACK_START_SPEED_THRESHOLD * averageSpeedIntoTack.getObject().getKnots()) {
            Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> potentiallyFlippedCluster = clusters.stream().max((a, b)->
            (int) Math.signum(Math.abs(a.getMean().getDifferenceTo(dominantCluster.getMean()).getDegrees())-
                  Math.abs(b.getMean().getDifferenceTo(dominantCluster.getMean()).getDegrees()))).get();
            for (ManeuverClassification potentiallyFlippedManeuverClassification : potentiallyFlippedCluster) {
                ManeuverClassification opposite = potentiallyFlippedManeuverClassification.getOpposite();
                
                // TODO the oppositeCluster holds candidates for wind direction flipping; however, if tacking and jibing angles are similar, we may have gotten it the wrong way around and need to swap clusters altogether
                // TODO decide this based on speed comparison: the boat should approach a jibe faster than a tack, assuming they were in similar wind conditions...
            }
        }
        Bearing mostLikelyCandidateForWindBearingSoFar = dominantCluster.getMean();
        // FIXME remove again when done with debugging
        int id=0;
        stringRepresentation.delete(0, stringRepresentation.length());
        stringRepresentation.append(ManeuverClassification.getToStringColumnHeaders());
        stringRepresentation.append('\n');
        stringRepresentation.append(ManeuverClassification.getToStringColumnTypes());
        stringRepresentation.append('\n');
        stringRepresentation.append(ManeuverClassification.getToStringColumnHeaders());
        stringRepresentation.append('\n');
        for (ManeuverClassification i : maneuverClassifications) {
            stringRepresentation.append(i.toString(""+id++));
            stringRepresentation.append('\n');
        }
    }
    
    public String getStringRepresentation() {
        return stringRepresentation.toString();
    }

    /**
     * For each maneuver from <code>maneuvers</code>, two hypothetical maneuver classifications are created: one that
     * assumes the maneuver happened on a {@link LegType#DOWNWIND downwind}, the other assuming the maneuver happened on
     * an {@link LegType#UPWIND upwind} leg. The maneuver's course change direction helps decide on which tack the
     * competitor sailed when starting the maneuver.
     * <p>
     * 
     * Each such hypothetical maneuver classification stores the leg type and tack, the maneuver's course change angle
     * and the speed and course before the maneuver. Additionally, from these (partly assumed) values, the
     * {@link #polarService} determines the average true wind angle and average true wind speed that the competitor
     * would have experienced at the beginning of the maneuver. The middle maneuver angle is used to infer the wind direction
     * under the assumption that the maneuver may have been a tack when on an upwind leg and a jibe when on a downwind leg.<p>
     */
    private Set<ManeuverClassification> getManeuverClassifications(final Map<Maneuver, Competitor> maneuvers) {
        Set<ManeuverClassification> maneuverClassifications = new HashSet<>();
        for (final Entry<Maneuver, Competitor> maneuverAndCompetitor : maneuvers.entrySet()) {
            // Now for each maneuver's starting speed (speed into the maneuver) get the approximated
            // wind speed and direction assuming average upwind / downwind performance based on the polar service.
            // for course changes to PORT use only UPWIND/PORT and DOWNWIND/STARBOARD and analogously for course changes to STARBOARD
            List<ManeuverClassification> currentManeuverClassifications = new ArrayList<>();
            List<ManeuverClassification> oppositeManeuverClassifications = null;
            // the following loop is executed exactly twice
            for (Pair<LegType, Tack> i : maneuverAndCompetitor.getKey().getDirectionChangeInDegrees() > 0 ?
                    Arrays.asList(new Pair<LegType, Tack>(LegType.UPWIND, Tack.STARBOARD), new Pair<LegType, Tack>(LegType.DOWNWIND, Tack.PORT)) :
                    Arrays.asList(new Pair<LegType, Tack>(LegType.UPWIND, Tack.PORT), new Pair<LegType, Tack>(LegType.DOWNWIND, Tack.STARBOARD))) {
                for (SpeedWithBearingWithConfidence<Void> s : polarService.getAverageTrueWindSpeedAndAngleCandidates(
                        maneuverAndCompetitor.getValue().getBoat().getBoatClass(),
                        maneuverAndCompetitor.getKey().getSpeedWithBearingBefore(), i.getA(), i.getB())) {
                    ManeuverClassification maneuverClassification = new ManeuverClassification(
                            maneuverAndCompetitor.getValue(), maneuverAndCompetitor.getKey(), i.getA(), i.getB(), s);
                    // note that for different leg types the polar service may return a different number of inverse TWA/TWS lookup results
                    if (oppositeManeuverClassifications != null && oppositeManeuverClassifications.size() > currentManeuverClassifications.size()) {
                        oppositeManeuverClassifications.get(currentManeuverClassifications.size()).setOpposite(maneuverClassification);
                        maneuverClassification.setOpposite(oppositeManeuverClassifications.get(currentManeuverClassifications.size()));
                    }
                    currentManeuverClassifications.add(maneuverClassification);
                    maneuverClassifications.add(maneuverClassification);
                }
                oppositeManeuverClassifications = currentManeuverClassifications;
                currentManeuverClassifications = new ArrayList<>();
            }
        }
        return maneuverClassifications;
    }

    private Map<Maneuver, Competitor> getAllManeuvers() {
        Map<Maneuver, Competitor> maneuvers = new HashMap<>();
        final Waypoint firstWaypoint = trackedRace.getRace().getCourse().getFirstWaypoint();
        final Waypoint lastWaypoint = trackedRace.getRace().getCourse().getLastWaypoint();
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            final TimePoint from = Util.getFirstNonNull(
                    firstWaypoint == null ? null : trackedRace.getMarkPassing(competitor, firstWaypoint) == null ? null : trackedRace.getMarkPassing(competitor, firstWaypoint).getTimePoint(),
                    trackedRace.getStartOfRace(), trackedRace.getStartOfTracking());
            final TimePoint to = Util.getFirstNonNull(
                    lastWaypoint == null ? null : trackedRace.getMarkPassing(competitor, lastWaypoint) == null ? null : trackedRace.getMarkPassing(competitor, lastWaypoint).getTimePoint(),
                    trackedRace.getEndOfRace() , trackedRace.getEndOfTracking(),
                    MillisecondsTimePoint.now());
            for (Maneuver maneuver : trackedRace.getManeuvers(competitor, from, to, /* waitForLatest */ false)) {
                maneuvers.put(maneuver, competitor);
            }
        }
        return Collections.unmodifiableMap(maneuvers);
    }
}
