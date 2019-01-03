package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelMetadata.DistanceValueRange;

public class DistanceBasedTwdTransitionRegressorModelFactory extends
        SingleDimensionBasedTwdTransitionRegressorModelFactory<DistanceBasedTwdTransitionRegressorModelMetadata> {

    @Override
    public List<DistanceBasedTwdTransitionRegressorModelMetadata> getAllValidContextSpecificModelMetadataCandidates(
            DistanceBasedTwdTransitionRegressorModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        List<DistanceBasedTwdTransitionRegressorModelMetadata> modelMetadatas = new ArrayList<>();
        for (DistanceValueRange distanceValueRange : DistanceValueRange.values()) {
            modelMetadatas.add(new DistanceBasedTwdTransitionRegressorModelMetadata(distanceValueRange));
        }
        return modelMetadatas;
    }

    @Override
    public DistanceBasedTwdTransitionRegressorModelMetadata createNewModelMetadata(TwdTransition twdTransition) {
        double metersPassed = twdTransition.getDistance().getMeters();
        for (DistanceValueRange distanceValueRange : DistanceValueRange.values()) {
            if (distanceValueRange.getFromInclusive() <= metersPassed
                    && distanceValueRange.getToExclusive() > metersPassed) {
                return new DistanceBasedTwdTransitionRegressorModelMetadata(distanceValueRange);
            }
        }
        throw new IllegalStateException(
                "No DistanceValueRange available for distance value of " + metersPassed + " meters");
    }

}
