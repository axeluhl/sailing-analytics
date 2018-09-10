package com.sap.sailing.windestimation;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractWindEstimatorImpl<T> implements WindEstimator<T> {

    @Override
    public List<WindWithConfidence<Void>> estimateWind(RaceWithEstimationData<T> raceWithEstimationData) {
        List<CompetitorTrackWithEstimationData<T>> filteredCompetitorTracks = filterOutImplausibleTracks(
                raceWithEstimationData.getCompetitorTracks());
        RaceWithEstimationData<T> newRace = new RaceWithEstimationData<>(raceWithEstimationData.getRegattaName(),
                raceWithEstimationData.getRaceName(), filteredCompetitorTracks);
        filterOutIrrelevantElementsFromCompetitorTracks(newRace);
        List<WindWithConfidence<Void>> windTrack = estimateWindByFilteredCompetitorTracks(newRace);
        return windTrack;
    }

    protected abstract void filterOutIrrelevantElementsFromCompetitorTracks(
            RaceWithEstimationData<T> raceWithEstimationData);

    protected abstract List<WindWithConfidence<Void>> estimateWindByFilteredCompetitorTracks(
            RaceWithEstimationData<T> raceWithEstimationData);

    public List<CompetitorTrackWithEstimationData<T>> filterOutImplausibleTracks(
            Iterable<CompetitorTrackWithEstimationData<T>> competitorTracks) {
        List<CompetitorTrackWithEstimationData<T>> result = new ArrayList<>();
        for (CompetitorTrackWithEstimationData<T> track : competitorTracks) {
            if (track.isClean()) {
                result.add(track);
            }
        }
        return result;
    }

}
