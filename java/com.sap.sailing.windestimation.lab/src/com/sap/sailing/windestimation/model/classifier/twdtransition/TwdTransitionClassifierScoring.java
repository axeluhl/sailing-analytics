package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.ClassifierScoring;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.classifier.twdtransition.TwdTransitionClassifierModelContext;

public class TwdTransitionClassifierScoring extends ClassifierScoring<TwdTransition, TwdTransitionClassifierModelContext> {

    public TwdTransitionClassifierScoring(
            TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext> trainedClassifierModel) {
        super(trainedClassifierModel, i -> i == 0 ? "Incorrect" : "Correct");
    }

}
