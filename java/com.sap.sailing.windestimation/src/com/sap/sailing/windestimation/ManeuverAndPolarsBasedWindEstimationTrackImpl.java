package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.EstimationDataUtil;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverAndPolarsBasedWindEstimationTrackImpl extends WindTrackImpl {

    private static final long serialVersionUID = 7134653811016476998L;
    /**
     * Using a fairly low base confidence for this estimation wind track. It shall only serve as a default and should be
     * superseded by other wind sources easily.
     */
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;

    private final TrackedRace trackedRace;
    private final ManeuverAndPolarsBasedWindEstimator windEstimator;
    private final PolarDataService polarDataService;

    public ManeuverAndPolarsBasedWindEstimationTrackImpl(ManeuverAndPolarsBasedWindEstimator windEstimator,
            TrackedRace trackedRace, PolarDataService polarDataService, long millisecondsOverWhichToAverage,
            boolean waitForLatest) {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */true,
                ManeuverAndPolarsBasedWindEstimationTrackImpl.class.getSimpleName());
        this.windEstimator = windEstimator;
        this.trackedRace = trackedRace;
        this.polarDataService = polarDataService;
    }

    public void analyzeRace() {
        List<CompetitorTrackWithEstimationData> competitorTracks = EstimationDataUtil
                .getCompetitorTracksWithEstimationData(trackedRace, polarDataService);
        List<WindWithConfidence<TimePoint>> windTrack = windEstimator.estimateWind(competitorTracks);
        for (WindWithConfidence<TimePoint> windWithConfidence : windTrack) {
            // TODO how to set confidence for each wind fix individually?
            add(windWithConfidence.getObject());
        }
    }

}
