package com.sap.sailing.windestimation.data.transformer;

import java.util.List;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer
        implements CompetitorTrackTransformer<CompleteManeuverCurveWithEstimationData, ManeuverForEstimation> {

    private final ManeuverForEstimationTransformer internalTransformer = new ManeuverForEstimationTransformer();

    @Override
    public List<ManeuverForEstimation> transformElements(
            CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrackWithElementsToTransform) {
        List<ConvertableToManeuverForEstimation> convertableManeuvers = ConvertableManeuverForEstimationAdapterForCompleteManeuverCurveWithEstimationData
                .getConvertableManeuvers(competitorTrackWithElementsToTransform.getElements());
        return internalTransformer.getManeuversForEstimation(convertableManeuvers,
                competitorTrackWithElementsToTransform.getBoatClass());
    }

}
