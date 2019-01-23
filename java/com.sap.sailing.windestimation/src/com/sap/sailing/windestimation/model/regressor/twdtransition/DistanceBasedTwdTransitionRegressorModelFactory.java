package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelMetadata.DistanceValueRange;

public class DistanceBasedTwdTransitionRegressorModelFactory extends
        SingleDimensionBasedTwdTransitionRegressorModelFactory<DistanceBasedTwdTransitionRegressorModelMetadata> {

    @Override
    public List<DistanceBasedTwdTransitionRegressorModelMetadata> getAllValidContextSpecificModelMetadataFeatureSupersets(
            DistanceBasedTwdTransitionRegressorModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        List<DistanceBasedTwdTransitionRegressorModelMetadata> modelMetadatas = new ArrayList<>();
        modelMetadatas.add(contextSpecificModelMetadataWithMaxFeatures);
        return modelMetadatas;
    }

    @Override
    public DistanceBasedTwdTransitionRegressorModelMetadata createNewModelMetadata(TwdTransition twdTransition) {
        double metersPassed = twdTransition.getDistance().getMeters();
        for (DistanceValueRange distanceValueRange : DistanceValueRange.values()) {
            DistanceBasedTwdTransitionRegressorModelMetadata modelMetadata = new DistanceBasedTwdTransitionRegressorModelMetadata(
                    distanceValueRange);
            if (modelMetadata.isDimensionValueSupported(metersPassed)) {
                return modelMetadata;
            }
        }
        throw new IllegalStateException(
                "No DistanceValueRange available for distance value of " + metersPassed + " meters");
    }

    @Override
    public List<DistanceBasedTwdTransitionRegressorModelMetadata> getAllValidContextSpecificModelMetadata() {
        List<DistanceBasedTwdTransitionRegressorModelMetadata> result = new ArrayList<>();
        for (DistanceValueRange distanceValueRange : DistanceValueRange.values()) {
            DistanceBasedTwdTransitionRegressorModelMetadata modelMetadata = new DistanceBasedTwdTransitionRegressorModelMetadata(
                    distanceValueRange);
            result.add(modelMetadata);
        }
        return result;
    }

}
