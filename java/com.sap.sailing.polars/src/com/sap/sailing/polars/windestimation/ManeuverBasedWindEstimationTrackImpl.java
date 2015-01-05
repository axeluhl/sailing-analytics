package com.sap.sailing.polars.windestimation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
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
        private final Maneuver maneuver;
        private final Map<Pair<LegType, Tack>, SpeedWithBearingWithConfidence<Void>> estimatedTrueWindSpeedAndAngles;
        protected ManeuverClassification(Maneuver maneuver,
                Map<Pair<LegType, Tack>, SpeedWithBearingWithConfidence<Void>> estimatedTrueWindSpeedAndAngles) {
            super();
            this.maneuver = maneuver;
            this.estimatedTrueWindSpeedAndAngles = estimatedTrueWindSpeedAndAngles;
        }
        public Maneuver getManeuver() {
            return maneuver;
        }
        public Map<Pair<LegType, Tack>, SpeedWithBearingWithConfidence<Void>> getEstimatedTrueWindSpeedAndAngles() {
            return estimatedTrueWindSpeedAndAngles;
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
            Map<Pair<LegType, Tack>, SpeedWithBearingWithConfidence<Void>> estimatedTrueWindSpeedAndAngles = new HashMap<>();
            for (LegType legType : new LegType[] { LegType.UPWIND, LegType.DOWNWIND }) {
                for (Tack tack : new Tack[] { Tack.PORT, Tack.STARBOARD }) {
                    SpeedWithBearingWithConfidence<Void> trueWindSpeedAndAngle = polarService
                            .getAverageTrueWindSpeedAndAngle(maneuverAndBoatClass.getValue(), maneuverAndBoatClass
                                    .getKey().getSpeedWithBearingBefore(), legType, tack);
                    estimatedTrueWindSpeedAndAngles.put(new Pair<>(legType, tack), trueWindSpeedAndAngle);
                }
            }
            ManeuverClassification maneuverClassification = new ManeuverClassification(maneuverAndBoatClass.getKey(), estimatedTrueWindSpeedAndAngles);
            maneuverClassifications.add(maneuverClassification);
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
