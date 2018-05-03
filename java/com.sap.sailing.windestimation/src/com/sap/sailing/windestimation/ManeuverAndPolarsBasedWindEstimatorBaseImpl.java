package com.sap.sailing.windestimation;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class ManeuverAndPolarsBasedWindEstimatorBaseImpl implements ManeuverAndPolarsBasedWindEstimator {

    private final PolarDataService polarService;

    public ManeuverAndPolarsBasedWindEstimatorBaseImpl(PolarDataService polarService) {
        this.polarService = polarService;
    }

    public List<WindWithConfidence<TimePoint>> estimateWind(
            Iterable<CompetitorTrackWithEstimationData> competitorTracks) {
        List<CompetitorTrackWithEstimationData> filteredCompetitorTracks = filterOutImplausibleTracks(competitorTracks);
        List<WindWithConfidence<TimePoint>> windTrack = estimateWindByFilteredCompetitorTracks(
                filteredCompetitorTracks);
        return windTrack;
    }

    protected abstract List<WindWithConfidence<TimePoint>> estimateWindByFilteredCompetitorTracks(
            List<CompetitorTrackWithEstimationData> filteredCompetitorTracks);

    public List<CompetitorTrackWithEstimationData> filterOutImplausibleTracks(
            Iterable<CompetitorTrackWithEstimationData> competitorTracks) {
        List<CompetitorTrackWithEstimationData> result = new ArrayList<>();
        for (CompetitorTrackWithEstimationData track : competitorTracks) {
            if (track.getAvgIntervalBetweenFixesInSeconds() <= 100.0 && track.getDuration().asSeconds() != 0
                    && track.getDistanceTravelled().getKilometers() / track.getDuration().asHours() >= 1.852) {
                result.add(track);
            }
        }
        return result;
    }

    public PolarDataService getPolarService() {
        return polarService;
    }

}
