package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;
import com.sap.sailing.windestimation.model.classifier.twdtransition.TwdTransitionClassifierModelMetadata;

public class LabelledTwdTransitionModelMetadata extends TwdTransitionClassifierModelMetadata
        implements LabelExtraction<TwdTransition> {

    private static final long serialVersionUID = 8198288811779220L;

    public LabelledTwdTransitionModelMetadata(BoatClass boatClass) {
        super(boatClass);
    }

    @Override
    public int getY(TwdTransition instance) {
        int label = ((LabelledTwdTransition) instance).isCorrect() ? 1 : 0;
        return label;
    }

}
