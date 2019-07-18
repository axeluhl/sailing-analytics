package com.sap.sailing.windestimation.data.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;

/**
 * Transforms a {@link CompetitorTrackWithEstimationData} with elements of one type into a new competitor track with
 * same metadata, but having its elements of another type.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <FromType>
 *            The type of elements from the original competitor track to transform
 * @param <ToType>
 *            The target type of elements to which the provided competitor track will be transformed
 */
public interface CompetitorTrackTransformer<FromType, ToType>
        extends Function<CompetitorTrackWithEstimationData<FromType>, CompetitorTrackWithEstimationData<ToType>> {

    List<ToType> transformElements(CompetitorTrackWithEstimationData<FromType> competitorTrackWithElementsToTransform);

    default CompetitorTrackWithEstimationData<ToType> transform(
            CompetitorTrackWithEstimationData<FromType> competitorTrackToTransform) {
        List<ToType> transformedElements = transformElements(competitorTrackToTransform);
        CompetitorTrackWithEstimationData<ToType> competitorTrack = competitorTrackToTransform
                .constructWithElements(transformedElements);
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
