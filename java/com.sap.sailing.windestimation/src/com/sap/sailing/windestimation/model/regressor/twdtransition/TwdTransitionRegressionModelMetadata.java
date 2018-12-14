package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.store.ContextType;

public class TwdTransitionRegressionModelMetadata extends ContextSpecificModelMetadata<TwdTransition> {

    private static final long serialVersionUID = 1120422671027132155L;

    public TwdTransitionRegressionModelMetadata() {
        super(ContextType.TWD_TRANSITION);
    }

    @Override
    public double[] getX(TwdTransition instance) {
        return new double[] { instance.getDistance().getMeters(), instance.getDuration().asSeconds() };
    }

    @Override
    public boolean isContainsAllFeatures(TwdTransition instance) {
        return true;
    }

    @Override
    public int getNumberOfInputFeatures() {
        return 2;
    }

    @Override
    public int getNumberOfPossibleTargetValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() {
        return "twdTransitionRegression";
    }

    @Override
    public int hashCode() {
        return getContextType().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof TwdTransitionRegressionModelMetadata) {
            return getContextType().equals(((TwdTransitionRegressionModelMetadata) obj).getContextType());
        }
        return false;
    }

    @Override
    public String toString() {
        return "TwdTransitionRegressionModelMetadata [getContextType()=" + getContextType() + "]";
    }

}
