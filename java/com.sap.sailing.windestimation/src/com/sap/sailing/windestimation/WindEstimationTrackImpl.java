package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationTrackImpl<T> extends WindTrackImpl {

    private static final long serialVersionUID = 7134653811016476998L;
    /**
     * Using a fairly low base confidence for this estimation wind track. It shall only serve as a default and should be
     * superseded by other wind sources easily.
     */
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;

    private final TrackedRace trackedRace;
    private final WindEstimator<T> windEstimator;
    private final PolarDataService polarDataService;
    private final CompetitorTracksFromTrackedRaceExtractor<T> competitorTracksExtractor;

    public WindEstimationTrackImpl(WindEstimator<T> windEstimator,
            CompetitorTracksFromTrackedRaceExtractor<T> competitorTracksExtractor, TrackedRace trackedRace,
            PolarDataService polarDataService, long millisecondsOverWhichToAverage, boolean waitForLatest) {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */true,
                WindEstimationTrackImpl.class.getSimpleName());
        this.windEstimator = windEstimator;
        this.competitorTracksExtractor = competitorTracksExtractor;
        this.trackedRace = trackedRace;
        this.polarDataService = polarDataService;
    }

    public void analyzeRace() {
        List<CompetitorTrackWithEstimationData<T>> competitorTracks = competitorTracksExtractor
                .extractCompetitorTracks(trackedRace, polarDataService);
        List<WindWithConfidence<ManeuverForEstimation>> windTrack = windEstimator.estimateWind(competitorTracks);
        for (WindWithConfidence<ManeuverForEstimation> windWithConfidence : windTrack) {
            // TODO how to set confidence for each wind fix individually?
            add(windWithConfidence.getObject());
        }
    }

}
