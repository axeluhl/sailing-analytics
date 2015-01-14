package com.sap.sailing.polars.windestimation;

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

    public ManeuverBasedWindEstimationTrackImpl(PolarDataService polarService, TrackedRace trackedRace, long millisecondsOverWhichToAverage)
            throws NotEnoughDataHasBeenAddedException {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */ false,
                /* nameForReadWriteLock */ ManeuverBasedWindEstimationTrackImpl.class.getName());
        this.polarService = polarService;
        this.trackedRace = trackedRace;
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
        
        protected ManeuverClassification(Competitor competitor,
                Maneuver maneuver, LegType legType, Tack tack, SpeedWithBearingWithConfidence<Void> estimatedTrueWindSpeedAndAngle) {
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
            return "competitor, timePoint, angleDeg, boatSpeedKn, cogDeg, windEstimationFromDeg, lossM, assumedLegType, assumedTack, estimatedTrueWindSpeedKn, estimatedTrueWindAngleDeg";
        }
        
        @Override
        public String toString() {
            final String prefix = "" + getCompetitor().getName() + ", " + getTimePoint() + ", " + getManeuverAngleDeg()
                    + ", " + getSpeedAtManeuverStart().getKnots() + ", "
                    + getSpeedAtManeuverStart().getBearing().getDegrees();
            final StringBuilder result = new StringBuilder();
            result.append(prefix);
            result.append(", ");
            result.append(getEstimatedWindBearing().reverse().getDegrees());
            result.append(", ");
            result.append(getManeuverLoss() == null ? 0.0 : getManeuverLoss().getMeters());
            result.append(", ");
            result.append(getLegType());
            result.append(", ");
            result.append(getTack());
            result.append(", ");
            result.append(getEstimatedTrueWindSpeedAndAngle().getObject().getKnots());
            result.append(", ");
            result.append(getEstimatedTrueWindSpeedAndAngle().getObject().getBearing().getDegrees());
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
     * line mark passing is found for that competitor yet) and {@link #getManeuverClassifications classified} based on
     * two hypotheses for each maneuver: the competitor was sailing upwind and the maneuver was a tack, or the
     * competitor was sailing downwind and the maneuver was a jibe, and the approximate true wind direction is the
     * middle course over ground between start and end of the maneuver (reversed for upwind beats). Obviously, at most
     * one of the two hypotheses can hold true for any maneuver. None of them applies if the maneuver was neither a tack
     * nor a jibe but a mark rounding or a significant head-up or bear-away maneuver.
     * <p>
     * 
     * To figure out if one of the two hypotheses holds, the hypotheses are clustered based on the approximated true
     * wind direction that follows from them. A confidence can be assigned to each hypothesis which is based on three
     * factors:
     * <ul>
     * <li>How close to the expected maneuver angle was the maneuver's actual course change?</li>
     * <li>How consistent with the average speeds for the two hypothetical maneuver types is the maneuver's actual
     * speed? This assumes that jibes on a downwind leg may have a characteristically higher speed at the beginning of
     * the maneuver when compared to a tack.</li>
     * <li>How well does the virtual wind fix inferred from the hypothesis fit into the local spatial and temporal wind
     * field resulting from the other virtual wind fixes? Of course, wind can theoretically shift by 180 degrees in
     * a short period of time or across a short distance, but it may not be all too likely.</li>
     * </ul>
     * 
     * 
     */
    private void analyzeRace() throws NotEnoughDataHasBeenAddedException {
        final Map<Maneuver, Competitor> maneuvers = getAllManeuvers();
        Set<ManeuverClassification> maneuverClassifications = getManeuverClassifications(maneuvers);
        // Now cluster the maneuvers by estimated maneuver angle
        // constrain the clustering to those maneuvers that are sufficiently close to the expected maneuver angle
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
        System.out.println(ManeuverClassification.getToStringColumnHeaders());
        for (ManeuverClassification i : maneuverClassifications) {
            System.out.println(i);
        }
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
