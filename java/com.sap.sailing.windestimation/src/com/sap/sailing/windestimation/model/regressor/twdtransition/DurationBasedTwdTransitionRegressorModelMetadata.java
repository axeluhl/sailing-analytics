package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;

public class DurationBasedTwdTransitionRegressorModelMetadata
        extends SingleDimensionBasedTwdTransitionRegressorModelMetadata {

    private static final String DIMENSION_NAME = "Duration";
    private static final long serialVersionUID = 4324543543l;

    public DurationBasedTwdTransitionRegressorModelMetadata() {
        super(DIMENSION_NAME);
    }

    @Override
    public double getDimensionValue(TwdTransition instance) {
        return instance.getDuration().asSeconds();
    }

}
