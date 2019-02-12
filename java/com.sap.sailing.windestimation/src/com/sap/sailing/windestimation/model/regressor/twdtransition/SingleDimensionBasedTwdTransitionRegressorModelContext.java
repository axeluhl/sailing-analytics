package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.store.ModelDomainType;

public abstract class SingleDimensionBasedTwdTransitionRegressorModelContext extends ModelContext<TwdTransition> {

    private static final long serialVersionUID = 20422671027132155L;
    private final String dimensionName;
    private final SupportedDimensionValueRange supportedDimensionValueRange;

    public SingleDimensionBasedTwdTransitionRegressorModelContext(String dimensionName,
            ModelDomainType persistenceContextType, SupportedDimensionValueRange supportedDimensionValueRange) {
        super(persistenceContextType);
        this.dimensionName = dimensionName;
        this.supportedDimensionValueRange = supportedDimensionValueRange;
    }

    public double[] getX(TwdTransition instance) {
        return new double[] { getDimensionValue(instance) };
    }

    public abstract double getDimensionValue(TwdTransition instance);

    public double getPreprocessedDimensionValue(double dimensionValue) {
        if (getSupportedDimensionValueRange().isSquareRootInput()) {
            return Math.sqrt(dimensionValue);
        }
        return dimensionValue;
    }

    public SupportedDimensionValueRange getSupportedDimensionValueRange() {
        return supportedDimensionValueRange;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dimensionName == null) ? 0 : dimensionName.hashCode());
        result = prime * result
                + ((supportedDimensionValueRange == null) ? 0 : supportedDimensionValueRange.hashCode());
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
        SingleDimensionBasedTwdTransitionRegressorModelContext other = (SingleDimensionBasedTwdTransitionRegressorModelContext) obj;
        if (dimensionName == null) {
            if (other.dimensionName != null)
                return false;
        } else if (!dimensionName.equals(other.dimensionName))
            return false;
        if (supportedDimensionValueRange == null) {
            if (other.supportedDimensionValueRange != null)
                return false;
        } else if (!supportedDimensionValueRange.equals(other.supportedDimensionValueRange))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return dimensionName + "BasedTwdTransitionRegressorModelContext [dimensionName=" + dimensionName
                + ", supportedDimensionValueRange=" + supportedDimensionValueRange + "]";
    }

    @Override
    public String getId() {
        return dimensionName + "BasedTwdTransitionRegressor" + "From" + supportedDimensionValueRange.getFromInclusive()
                + "To"
                + (supportedDimensionValueRange.getToExclusive() >= SupportedDimensionValueRange.MAX_VALUE ? "Maximum"
                        : supportedDimensionValueRange.getToExclusive());
    }

    public boolean isDimensionValueSupported(double dimensionValue) {
        if (supportedDimensionValueRange.getFromInclusive() <= dimensionValue
                && (supportedDimensionValueRange.getToExclusive() > dimensionValue
                        || dimensionValue >= SupportedDimensionValueRange.MAX_VALUE)) {
            return true;
        }
        return false;
    }

    public boolean isDimensionValueSupportedForTraining(double dimensionValue) {
        return isDimensionValueSupported(dimensionValue)
                || supportedDimensionValueRange.getToExclusive() == dimensionValue;
    }

    @Override
    public boolean isContainsAllFeatures(TwdTransition instance) {
        double x = getDimensionValue(instance);
        return isDimensionValueSupported(x);
    }

}
