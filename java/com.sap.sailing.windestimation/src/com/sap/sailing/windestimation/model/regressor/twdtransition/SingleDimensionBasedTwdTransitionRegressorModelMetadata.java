package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.store.ContextType;

public abstract class SingleDimensionBasedTwdTransitionRegressorModelMetadata
        extends ContextSpecificModelMetadata<TwdTransition> {

    private static final long serialVersionUID = 20422671027132155L;
    private final String dimensionName;

    public SingleDimensionBasedTwdTransitionRegressorModelMetadata(String dimensionName) {
        super(ContextType.TWD_TRANSITION);
        this.dimensionName = dimensionName;
    }

    public double[] getX(TwdTransition instance) {
        return new double[] { getDimensionValue(instance) };
    }

    public abstract double getDimensionValue(TwdTransition instance);

    @Override
    public boolean isContainsAllFeatures(TwdTransition instance) {
        return true;
    }

    @Override
    public int getNumberOfInputFeatures() {
        return 1;
    }

    @Override
    public int getNumberOfPossibleTargetValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(getClass())) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return dimensionName + "BasedTwdTransitionRegressorModelMetadata [getContextType()=" + getContextType() + "]";
    }

    @Override
    public String getId() {
        return dimensionName + "BasedTwdTransitionRegressor";
    }

}
