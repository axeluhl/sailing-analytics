package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;

public class TwdTransitionLabelExtraction implements LabelExtraction<LabelledTwdTransition> {

    @Override
    public int getY(LabelledTwdTransition instance) {
        int label = instance.isCorrect() ? 1 : 0;
        return label;
    }

}
