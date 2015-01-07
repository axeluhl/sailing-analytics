package com.sap.sailing.polars.windestimation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
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
        /**
         * Course change implied by the maneuver
         */
        private final double maneuverAngleDeg;
        private final SpeedWithBearing speedAtManeuverStart;
        private final Bearing windEstimationIfTack;
        private final Bearing windEstimationIfJibe;
        private final Distance maneuverLoss;
        private final Map<Pair<LegType, Tack>, Set<SpeedWithBearingWithConfidence<Void>>> estimatedTrueWindSpeedAndAngles;
        
        protected ManeuverClassification(Maneuver maneuver,
                Map<Pair<LegType, Tack>, Set<SpeedWithBearingWithConfidence<Void>>> estimatedTrueWindSpeedAndAngles) {
            super();
            this.estimatedTrueWindSpeedAndAngles = estimatedTrueWindSpeedAndAngles;
            this.maneuverAngleDeg = maneuver.getDirectionChangeInDegrees();
            this.speedAtManeuverStart = maneuver.getSpeedWithBearingBefore();
            this.windEstimationIfJibe = maneuver.getSpeedWithBearingBefore().getBearing().middle(maneuver.getSpeedWithBearingAfter().getBearing());
            this.windEstimationIfTack = windEstimationIfJibe.reverse();
            this.maneuverLoss = maneuver.getManeuverLoss();
        }
        
        public double getManeuverAngleDeg() {
            return maneuverAngleDeg;
        }

        public SpeedWithBearing getSpeedAtManeuverStart() {
            return speedAtManeuverStart;
        }

        public Bearing getWindEstimationIfTack() {
            return windEstimationIfTack;
        }

        public Bearing getWindEstimationIfJibe() {
            return windEstimationIfJibe;
        }

        public Distance getManeuverLoss() {
            return maneuverLoss;
        }

        public Map<Pair<LegType, Tack>, Set<SpeedWithBearingWithConfidence<Void>>> getEstimatedTrueWindSpeedAndAngles() {
            return estimatedTrueWindSpeedAndAngles;
        }
        
        public static String getToStringColumnHeaders() {
            return "angleDeg, boatSpeedKn, cogDeg, windEstimationIfTack, windEstimationIfJibe, lossM, assumedLegType, assumedTack, estimatedTrueWindSpeedKn, estimatedTrueWindAngleDeg";
        }
        
        @Override
        public String toString() {
            final String prefix = "" + getManeuverAngleDeg() + ", " + getSpeedAtManeuverStart().getKnots()+", "+getSpeedAtManeuverStart().getBearing().getDegrees() +
                    ", " + getWindEstimationIfTack().getDegrees() + ", " + getWindEstimationIfJibe().getDegrees() + ", " + (getManeuverLoss()==null?0.0:getManeuverLoss().getMeters());
            final StringBuilder result = new StringBuilder();
            for (Entry<Pair<LegType, Tack>, Set<SpeedWithBearingWithConfidence<Void>>> i : getEstimatedTrueWindSpeedAndAngles().entrySet()) {
                for (SpeedWithBearingWithConfidence<Void> s : i.getValue()) {
                    result.append(prefix);
                    result.append(", ");
                    result.append(i.getKey().getA());
                    result.append(", ");
                    result.append(i.getKey().getB());
                    result.append(", ");
                    result.append(s.getObject().getKnots());
                    result.append(", ");
                    result.append(s.getObject().getBearing().getDegrees());
                    result.append("\n");
                }
            }
            return result.toString();
        }
    }
    
    /**
     * Fetches all the race's maneuvers and tries to find the tacks and jibes. For each such maneuver identified, a wind fix
     * will be created based on the average COG into and out of the maneuver.
     */
    private void analyzeRace() throws NotEnoughDataHasBeenAddedException {
        final Map<Maneuver, BoatClass> maneuvers = getAllManeuvers();
        Set<ManeuverClassification> maneuverClassifications = new HashSet<>();
        for (final Entry<Maneuver, BoatClass> maneuverAndBoatClass : maneuvers.entrySet()) {
            // Now for each maneuver's starting speed (speed into the maneuver) get the approximated
            // wind speed and direction assuming average upwind / downwind performance based on the polar service.
            // TODO continue here, asking the polarService for the wind speed based on boat speed, leg type and tack
            Map<Pair<LegType, Tack>, Set<SpeedWithBearingWithConfidence<Void>>> estimatedTrueWindSpeedAndAngles = new HashMap<>();
            // for course changes to PORT use only UPWIND/PORT and DOWNWIND/STARBOARD and analogously for course changes to STARBOARD
            for (Pair<LegType, Tack> i : maneuverAndBoatClass.getKey().getDirectionChangeInDegrees() > 0 ? Arrays
                    .asList(new Pair<LegType, Tack>(LegType.UPWIND, Tack.STARBOARD), new Pair<LegType, Tack>(
                            LegType.DOWNWIND, Tack.PORT)) : Arrays.asList(new Pair<LegType, Tack>(LegType.UPWIND,
                    Tack.PORT), new Pair<LegType, Tack>(LegType.DOWNWIND, Tack.STARBOARD))) {
                Set<SpeedWithBearingWithConfidence<Void>> trueWindSpeedsAndAngles = polarService
                        .getAverageTrueWindSpeedAndAngleCandidates(maneuverAndBoatClass.getValue(),
                                maneuverAndBoatClass.getKey().getSpeedWithBearingBefore(), i.getA(), i.getB());
                estimatedTrueWindSpeedAndAngles.put(new Pair<>(i.getA(), i.getB()), trueWindSpeedsAndAngles);
            }
            ManeuverClassification maneuverClassification = new ManeuverClassification(maneuverAndBoatClass.getKey(), estimatedTrueWindSpeedAndAngles);
            maneuverClassifications.add(maneuverClassification);
        }
        // FIXME remove again when done with debugging
        System.out.println(ManeuverClassification.getToStringColumnHeaders());
        for (ManeuverClassification i : maneuverClassifications) {
            System.out.println(i);
        }
    }

    private Map<Maneuver, BoatClass> getAllManeuvers() {
        Map<Maneuver, BoatClass> maneuvers = new HashMap<>();
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            final BoatClass boatClass = competitor.getBoat().getBoatClass();
            final TimePoint from = trackedRace.getStartOfRace() == null ? trackedRace.getStartOfTracking()
                    : trackedRace.getStartOfRace();
            final TimePoint to = trackedRace.getEndOfRace() == null ? trackedRace.getEndOfTracking() == null ?
                    MillisecondsTimePoint.now() : trackedRace.getEndOfTracking() : trackedRace.getEndOfRace();
            for (Maneuver maneuver : trackedRace.getManeuvers(competitor, from, to, /* waitForLatest */ false)) {
                maneuvers.put(maneuver, boatClass);
            }
        }
        return Collections.unmodifiableMap(maneuvers);
    }
}
