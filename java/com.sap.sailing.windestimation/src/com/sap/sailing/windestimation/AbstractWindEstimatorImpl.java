package com.sap.sailing.windestimation;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedImpl;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractWindEstimatorImpl<T> implements WindEstimator<T> {

    private final PolarDataService polarService;

    public AbstractWindEstimatorImpl(PolarDataService polarService) {
        this.polarService = polarService;
    }

    public List<WindWithConfidence<TimePoint>> estimateWind(
            Iterable<CompetitorTrackWithEstimationData<T>> competitorTracks) {
        List<CompetitorTrackWithEstimationData<T>> filteredCompetitorTracks = filterOutImplausibleTracks(
                competitorTracks);
        List<WindWithConfidence<TimePoint>> windTrack = estimateWindByFilteredCompetitorTracks(
                filteredCompetitorTracks);
        return windTrack;
    }

    protected abstract List<WindWithConfidence<TimePoint>> estimateWindByFilteredCompetitorTracks(
            List<CompetitorTrackWithEstimationData<T>> filteredCompetitorTracks);

    public List<CompetitorTrackWithEstimationData<T>> filterOutImplausibleTracks(
            Iterable<CompetitorTrackWithEstimationData<T>> competitorTracks) {
        List<CompetitorTrackWithEstimationData<T>> result = new ArrayList<>();
        for (CompetitorTrackWithEstimationData<T> track : competitorTracks) {
            if (track.getAvgIntervalBetweenFixesInSeconds() < 8
                    && new MeterPerSecondSpeedImpl(track.getDistanceTravelled().getMeters()
                            / track.getTrackStartTimePoint().until(track.getTrackEndTimePoint()).asSeconds())
                                    .getKnots() > 1) {
                result.add(track);
            }
        }
        return result;
    }

    public PolarDataService getPolarService() {
        return polarService;
    }

}
