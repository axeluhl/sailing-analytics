package com.sap.sailing.polars.windestimation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
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
            // TODO continue here, asking the polarService for the wind speed based on boat speed, leg type and tack
            Map<Pair<LegType, Tack>, Set<SpeedWithBearingWithConfidence<Void>>> estimatedTrueWindSpeedAndAngles = new HashMap<>();
            // for course changes to PORT use only UPWIND/PORT and DOWNWIND/STARBOARD and analogously for course changes to STARBOARD
            for (Pair<LegType, Tack> i : maneuverAndCompetitor.getKey().getDirectionChangeInDegrees() > 0 ? Arrays
                    .asList(new Pair<LegType, Tack>(LegType.UPWIND, Tack.STARBOARD), new Pair<LegType, Tack>(
                            LegType.DOWNWIND, Tack.PORT)) : Arrays.asList(new Pair<LegType, Tack>(LegType.UPWIND,
                    Tack.PORT), new Pair<LegType, Tack>(LegType.DOWNWIND, Tack.STARBOARD))) {
                Set<SpeedWithBearingWithConfidence<Void>> trueWindSpeedsAndAngles = polarService
                        .getAverageTrueWindSpeedAndAngleCandidates(maneuverAndCompetitor.getValue().getBoat().getBoatClass(),
                                maneuverAndCompetitor.getKey().getSpeedWithBearingBefore(), i.getA(), i.getB());
                estimatedTrueWindSpeedAndAngles.put(new Pair<>(i.getA(), i.getB()), trueWindSpeedsAndAngles);
            }
            for (Entry<Pair<LegType, Tack>, Set<SpeedWithBearingWithConfidence<Void>>> i : estimatedTrueWindSpeedAndAngles.entrySet()) {
                for (SpeedWithBearingWithConfidence<Void> s : i.getValue()) {
                    ManeuverClassification maneuverClassification = new ManeuverClassification(
                            maneuverAndCompetitor.getValue(), maneuverAndCompetitor.getKey(),
                            i.getKey().getA(), i.getKey().getB(), s);
                    maneuverClassifications.add(maneuverClassification);
                }
            }
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
