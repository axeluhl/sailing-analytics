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
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
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

        public double getManeuverAngleDeg() {
            return maneuverAngleDeg;
        }

        public SpeedWithBearing getSpeedAtManeuverStart() {
            return speedAtManeuverStart;
        }

        public Bearing getMiddleManeuverCourse() {
            return middleManeuverCourse;
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
            if (getLegType() == LegType.UPWIND) {
                result.append(getMiddleManeuverCourse().getDegrees());
            } else {
                result.append(getMiddleManeuverCourse().reverse().getDegrees());
            }
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
     * Fetches all the race's maneuvers and tries to find the tacks and jibes. For each such maneuver identified, a wind fix
     * will be created based on the average COG into and out of the maneuver.
     */
    private void analyzeRace() throws NotEnoughDataHasBeenAddedException {
        final Map<Maneuver, Competitor> maneuvers = getAllManeuvers();
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
        // Now cluster the maneuvers by estimated maneuver angle
        // constrain the clustering to those maneuvers that are sufficiently close to the expected maneuver angle
        KMeansMappingClusterer<ManeuverClassification, DoublePair, Bearing, ScalableBearing> clusterer =
                new KMeansMappingClusterer<>(4,
                        maneuverClassifications.stream().filter(
                                (mc)->Math.abs(mc.getEstimatedTrueWindSpeedAndAngle().getObject().getBearing().getDegrees()*2)-Math.abs(mc.getManeuverAngleDeg())<
                                    MANEUVER_ANGLE_DEVIATION_IN_DEGREES_THRESHOLD),
                                (m)->new ScalableBearing(m.getMiddleManeuverCourse()),
                                // use an evenly distributed set of cluster seeds for clustering wind direction estimations
                                Arrays.<Bearing>asList(new DegreeBearingImpl(0), new DegreeBearingImpl(90), new DegreeBearingImpl(180), new DegreeBearingImpl(270)).stream());
        final Set<Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing>> clusters = clusterer.getClusters();
        Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> dominantCluster = clusters.stream().max((a, b)->a.size()-b.size()).get();
        Pair<Set<SpeedWithConfidence<Void>>, Set<SpeedWithConfidence<Void>>> averageSpeedsIntoTacksAndJibes = dominantCluster.stream().collect(
                ()->new Pair<Set<SpeedWithConfidence<Void>>, Set<SpeedWithConfidence<Void>>>(new HashSet<>(), new HashSet<>()),
                (resultSoFar, mc)->{
                    if (mc.getLegType() == LegType.UPWIND) {
                        resultSoFar.getA().add(new SpeedWithConfidenceImpl<Void>(mc.getSpeedAtManeuverStart(), /* TODO confidence */ 1.0, /* relative to */ null));
                    } else { // DOWNWIND
                        resultSoFar.getB().add(new SpeedWithConfidenceImpl<Void>(mc.getSpeedAtManeuverStart(), /* TODO confidence */ 1.0, /* relative to */ null));
                    }
                },
                (s1, s2)->{ s1.getA().addAll(s2.getA()); s1.getB().addAll(s2.getB()); });
        Cluster<ManeuverClassification, DoublePair, Bearing, ScalableBearing> potentiallyFlippedCluster = clusters.stream().max((a, b)->
                    (int) Math.signum(Math.abs(a.getMean().getDifferenceTo(dominantCluster.getMean()).getDegrees())-
                          Math.abs(b.getMean().getDifferenceTo(dominantCluster.getMean()).getDegrees()))).get();
        Bearing mostLikelyCandidateForWindBearingSoFar = dominantCluster.getMean();
        for (ManeuverClassification potentiallyFlippedManeuverClassification : potentiallyFlippedCluster) {
            
            // TODO the oppositeCluster holds candidates for wind direction flipping; however, if tacking and jibing angles are similar, we may have gotten it the wrong way around and need to swap clusters altogether
            // TODO decide this based on speed comparison: the boat should approach a jibe faster than a tack, assuming they were in similar wind conditions...
        }
        // FIXME remove again when done with debugging
        System.out.println(ManeuverClassification.getToStringColumnHeaders());
        for (ManeuverClassification i : maneuverClassifications) {
            System.out.println(i);
        }
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
