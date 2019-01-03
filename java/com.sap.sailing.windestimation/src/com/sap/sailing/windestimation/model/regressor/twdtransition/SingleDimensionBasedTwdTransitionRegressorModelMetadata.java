package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.store.ContextType;

public abstract class SingleDimensionBasedTwdTransitionRegressorModelMetadata
        extends ContextSpecificModelMetadata<TwdTransition> {

    private static final long serialVersionUID = 20422671027132155L;
    private final String dimensionName;
    private final int polynomialDegree;
    private final boolean withBias;

    public SingleDimensionBasedTwdTransitionRegressorModelMetadata(String dimensionName, int polynomialDegree,
            boolean withBias) {
        super(ContextType.TWD_TRANSITION);
        this.dimensionName = dimensionName;
        this.polynomialDegree = polynomialDegree;
        this.withBias = withBias;
    }

    public double[] getX(TwdTransition instance) {
        return new double[] { getDimensionValue(instance) };
    }

    public abstract double getDimensionValue(TwdTransition instance);

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dimensionName == null) ? 0 : dimensionName.hashCode());
        result = prime * result + polynomialDegree;
        result = prime * result + (withBias ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SingleDimensionBasedTwdTransitionRegressorModelMetadata other = (SingleDimensionBasedTwdTransitionRegressorModelMetadata) obj;
        if (dimensionName == null) {
            if (other.dimensionName != null)
                return false;
        } else if (!dimensionName.equals(other.dimensionName))
            return false;
        if (polynomialDegree != other.polynomialDegree)
            return false;
        if (withBias != other.withBias)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return dimensionName + "BasedTwdTransitionRegressorModelMetadata [dimensionName=" + dimensionName
                + ", polynomialDegree=" + polynomialDegree + ", withBias=" + withBias
                + ", getSupportedDimensionValueRangeId()=" + getSupportedDimensionValueRangeId() + "]";
    }

    @Override
    public String getId() {
        return dimensionName + "BasedTwdTransitionRegressor" + getSupportedDimensionValueRangeId();
    }

    protected abstract String getSupportedDimensionValueRangeId();

    public int getPolynomialDegree() {
        return polynomialDegree;
    }

    public boolean isWithBias() {
        return withBias;
    }

    public abstract boolean isDimensionValueSupported(double dimensionValue);

    @Override
    public boolean isContainsAllFeatures(TwdTransition instance) {
        double x = getDimensionValue(instance);
        return isDimensionValueSupported(x);
    }

}
