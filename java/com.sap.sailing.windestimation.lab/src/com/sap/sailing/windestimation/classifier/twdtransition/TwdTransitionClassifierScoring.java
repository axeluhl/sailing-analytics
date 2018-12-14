package com.sap.sailing.windestimation.classifier.twdtransition;

import com.sap.sailing.windestimation.classifier.ClassifierScoring;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.data.TwdTransition;

public class TwdTransitionClassifierScoring extends ClassifierScoring<TwdTransition, TwdTransitionClassifierModelMetadata> {

    public TwdTransitionClassifierScoring(
            TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> trainedClassifierModel) {
        super(trainedClassifierModel, i -> i == 0 ? "Incorrect" : "Correct");
    }

}
