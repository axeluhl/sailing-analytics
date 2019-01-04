package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;

public class LabelledTwdTransitionClassifierModelMetadata extends TwdTransitionClassifierModelMetadata
        implements LabelExtraction<TwdTransition> {

    private static final long serialVersionUID = 8198288811779220L;

    public LabelledTwdTransitionClassifierModelMetadata(ManeuverTypeTransition maneuverTypeTransition) {
        super(maneuverTypeTransition);
    }

    @Override
    public int getY(TwdTransition instance) {
        int label = ((LabelledTwdTransition) instance).isCorrect() ? 1 : 0;
        return label;
    }

}
