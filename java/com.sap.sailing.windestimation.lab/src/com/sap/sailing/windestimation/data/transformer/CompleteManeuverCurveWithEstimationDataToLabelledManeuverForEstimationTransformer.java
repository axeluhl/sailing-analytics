package com.sap.sailing.windestimation.data.transformer;

import java.util.List;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;

public class CompleteManeuverCurveWithEstimationDataToLabelledManeuverForEstimationTransformer
        implements CompetitorTrackTransformer<CompleteManeuverCurveWithEstimationData, LabelledManeuverForEstimation> {

    private final LabelledManeuverForEstimationTransformer internalTransformer = new LabelledManeuverForEstimationTransformer();

    @Override
    public List<LabelledManeuverForEstimation> transformElements(
            CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrackWithElementsToTransform) {
        List<ConvertableToLabelledManeuverForEstimation> convertableManeuvers = ConvertableManeuverForEstimationAdapterForCompleteManeuverCurveWithEstimationData
                .getConvertableManeuvers(competitorTrackWithElementsToTransform.getElements());
        return internalTransformer.getManeuversForEstimation(convertableManeuvers,
                competitorTrackWithElementsToTransform.getBoatClass(),
                competitorTrackWithElementsToTransform.getRegattaName());
    }

}
