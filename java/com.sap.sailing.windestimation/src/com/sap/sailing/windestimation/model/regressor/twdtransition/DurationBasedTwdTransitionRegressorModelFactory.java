package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionRegressorModelMetadata.DurationValueRange;

public class DurationBasedTwdTransitionRegressorModelFactory extends
        SingleDimensionBasedTwdTransitionRegressorModelFactory<DurationBasedTwdTransitionRegressorModelMetadata> {

    @Override
    public List<DurationBasedTwdTransitionRegressorModelMetadata> getAllValidContextSpecificModelMetadataCandidates(
            DurationBasedTwdTransitionRegressorModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        List<DurationBasedTwdTransitionRegressorModelMetadata> modelMetadatas = new ArrayList<>();
        for (DurationValueRange durationValueRange : DurationValueRange.values()) {
            modelMetadatas.add(new DurationBasedTwdTransitionRegressorModelMetadata(durationValueRange));
        }
        return modelMetadatas;
    }

    @Override
    public DurationBasedTwdTransitionRegressorModelMetadata createNewModelMetadata(TwdTransition twdTransition) {
        double secondsPassed = twdTransition.getDuration().asSeconds();
        for (DurationValueRange durationValueRange : DurationValueRange.values()) {
            if (durationValueRange.getFromInclusive() <= secondsPassed
                    && durationValueRange.getToExclusive() > secondsPassed) {
                return new DurationBasedTwdTransitionRegressorModelMetadata(durationValueRange);
            }
        }
        throw new IllegalStateException(
                "No DurationValueRange available for duration value of " + secondsPassed + " seconds");
    }

}
