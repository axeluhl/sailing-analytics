package com.sap.sailing.windestimation.data.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;

public interface CompetitorTrackTransformer<FromType, ToType>
        extends Function<CompetitorTrackWithEstimationData<FromType>, CompetitorTrackWithEstimationData<ToType>> {

    List<ToType> transformElements(CompetitorTrackWithEstimationData<FromType> competitorTrackWithElementsToTransform);

    default CompetitorTrackWithEstimationData<ToType> transform(
            CompetitorTrackWithEstimationData<FromType> competitorTrackToTransform) {
        List<ToType> transformedElements = transformElements(competitorTrackToTransform);
        CompetitorTrackWithEstimationData<ToType> competitorTrack = new CompetitorTrackWithEstimationData<>(
                competitorTrackToTransform.getCompetitorName(), competitorTrackToTransform.getBoatClass(),
                transformedElements, competitorTrackToTransform.getAvgIntervalBetweenFixesInSeconds(),
                competitorTrackToTransform.getDistanceTravelled(), competitorTrackToTransform.getTrackStartTimePoint(),
                competitorTrackToTransform.getTrackEndTimePoint(), competitorTrackToTransform.getFixesCountForPolars(),
                competitorTrackToTransform.getMarkPassingsCount(), competitorTrackToTransform.getWaypointsCount());
        return competitorTrack;
    }

    default List<CompetitorTrackWithEstimationData<ToType>> transform(
            List<CompetitorTrackWithEstimationData<FromType>> competitorTracksWithManeuverEstimationData) {
        List<CompetitorTrackWithEstimationData<ToType>> competitorTracks = new ArrayList<>();
        for (CompetitorTrackWithEstimationData<FromType> otherCompetitorTrack : competitorTracksWithManeuverEstimationData) {
            CompetitorTrackWithEstimationData<ToType> transformedCompetitorTrack = transform(otherCompetitorTrack);
            competitorTracks.add(transformedCompetitorTrack);
        }
        return competitorTracks;
    }

    @Override
    default CompetitorTrackWithEstimationData<ToType> apply(
            CompetitorTrackWithEstimationData<FromType> competitorTrack) {
        return transform(competitorTrack);
    }

}
