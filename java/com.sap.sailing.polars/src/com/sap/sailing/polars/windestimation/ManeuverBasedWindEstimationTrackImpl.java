package com.sap.sailing.polars.windestimation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sse.common.TimePoint;
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

    public ManeuverBasedWindEstimationTrackImpl(PolarDataService polarService, TrackedRace trackedRace, long millisecondsOverWhichToAverage) {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */ false,
                /* nameForReadWriteLock */ ManeuverBasedWindEstimationTrackImpl.class.getName());
        this.polarService = polarService;
        this.trackedRace = trackedRace;
        analyzeRace();
    }

    /**
     * Fetches all the race's maneuvers and tries to find the tacks and jibes. For each such maneuver identified, a wind fix
     * will be created based on the average COG into and out of the maneuver.
     */
    private void analyzeRace() {
        final Map<Maneuver, BoatClass> maneuvers = getAllManeuvers();
        for (final Entry<Maneuver, BoatClass> maneuverAndBoatClass : maneuvers.entrySet()) {
            // Now for each maneuver's starting speed (speed into the maneuver) get the approximated
            // wind speed and direction assuming average upwind / downwind performance based on the polar service.
            PolarSheetsData polarSheet = polarService.getPolarSheetForBoatClass(maneuverAndBoatClass.getValue());
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
