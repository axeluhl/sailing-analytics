package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.LabeledTwdTransition;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;

public class TwdTransitionLabelExtraction implements LabelExtraction<LabeledTwdTransition> {

    @Override
    public int getY(LabeledTwdTransition instance) {
        int label = instance.isCorrect() ? 1 : 0;
        return label;
    }

}
