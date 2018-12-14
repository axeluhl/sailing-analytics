package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.ClassificationResultMapper;

public class TwdTransitionClassificationResultMapper implements
        ClassificationResultMapper<TwdTransition, TwdTransitionClassifierModelMetadata, TwdTransitionClassificationResult> {

    @Override
    public TwdTransitionClassificationResult mapToClassificationResult(double[] likelihoods, TwdTransition instance,
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadata) {
        TwdTransitionClassificationResult result = new TwdTransitionClassificationResult(likelihoods[1],
                likelihoods[0]);
        return result;
    }

}
