package com.sap.sailing.windestimation;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.windestimation.impl.CompetitorManeuverBasedWindDirectionEstimator;
import com.sap.sailing.windestimation.impl.WindDirectionCandidatesForTimePoint;

public class ManeuverAndPolarsBasedWindEstimationTrackImpl extends WindTrackImpl {

    private static final long serialVersionUID = 7134653811016476998L;
    /**
     * Using a fairly low base confidence for this estimation wind track. It shall only serve as a default and should be
     * superseded by other wind sources easily.
     */
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;

    private final PolarDataService polarService;
    private final TrackedRace trackedRace;

    public ManeuverAndPolarsBasedWindEstimationTrackImpl(PolarDataService polarService, TrackedRace trackedRace,
            long millisecondsOverWhichToAverage, boolean waitForLatest) {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */false, ManeuverAndPolarsBasedWindEstimationTrackImpl.class.getSimpleName());
        this.polarService = polarService;
        this.trackedRace = trackedRace;
    }
    
    public void analyzeRace() {
        Iterable<Competitor> competitors = trackedRace.getRace().getCompetitors();
        CompetitorManeuverBasedWindDirectionEstimator competitorManeuverBasedWindEstimator = new CompetitorManeuverBasedWindDirectionEstimator();
        List<Iterable<WindDirectionCandidatesForTimePoint>> windDirectionCandidatesPerCompetitorTrack = new ArrayList<>();
        for (Competitor competitor : competitors) {
            Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
            Iterable<WindDirectionCandidatesForTimePoint> windDirectionCandidates = competitorManeuverBasedWindEstimator.computeWindDirectionCandidates(maneuvers);
            windDirectionCandidatesPerCompetitorTrack.add(windDirectionCandidates);
        }
        //TODO go through all available time points and match the best/averaged/candidate
    }
    
}
