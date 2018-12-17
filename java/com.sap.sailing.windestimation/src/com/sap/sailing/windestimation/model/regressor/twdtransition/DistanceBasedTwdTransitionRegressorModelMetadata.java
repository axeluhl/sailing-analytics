package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;

public class DistanceBasedTwdTransitionRegressorModelMetadata
        extends SingleDimensionBasedTwdTransitionRegressorModelMetadata {

    private static final String DIMENSION_NAME = "Distance";
    private static final long serialVersionUID = 4324543543l;

    public DistanceBasedTwdTransitionRegressorModelMetadata() {
        super(DIMENSION_NAME);
    }

    @Override
    public double getDimensionValue(TwdTransition instance) {
        return instance.getDistance().getMeters();
    }

}
