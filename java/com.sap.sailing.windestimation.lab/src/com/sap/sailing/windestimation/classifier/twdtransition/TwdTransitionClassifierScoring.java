package com.sap.sailing.windestimation.classifier.twdtransition;

import com.sap.sailing.windestimation.classifier.ClassifierScoring;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.classifier.twdtransition.TwdTransitionClassifierModelMetadata;

public class TwdTransitionClassifierScoring extends ClassifierScoring<TwdTransition, TwdTransitionClassifierModelMetadata> {

    public TwdTransitionClassifierScoring(
            TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> trainedClassifierModel) {
        super(trainedClassifierModel, i -> i == 0 ? "Incorrect" : "Correct");
    }

}
