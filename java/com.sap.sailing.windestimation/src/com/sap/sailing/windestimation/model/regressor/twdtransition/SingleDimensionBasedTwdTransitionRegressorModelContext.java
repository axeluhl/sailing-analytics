package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.store.ModelDomainType;

/**
 * Base class for {@link DistanceBasedTwdTransitionRegressorModelContext} and
 * {@link DurationBasedTwdTransitionRegressorModelContext} containing common implementation of {@link ModelContext}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class SingleDimensionBasedTwdTransitionRegressorModelContext extends ModelContext<TwdTransition> {

    private static final long serialVersionUID = 20422671027132155L;
    private final String dimensionName;
    private final SupportedDimensionValueRange supportedDimensionValueRange;

    /**
     * Constructs a new model context for the provided value interval.
     * 
     * @param dimensionName
     *            The name of dimension, e.g. "Distance" or "Duration". It is used in {@link #getId()},
     *            {@link #toString()} and {@link #equals(Object)}.
     * @param domainType
     *            The domain type associated with the model of this model context implementation
     * @param supportedDimensionValueRange
     *            The value interval which is represented by this context
     */
    public SingleDimensionBasedTwdTransitionRegressorModelContext(String dimensionName, ModelDomainType domainType,
            SupportedDimensionValueRange supportedDimensionValueRange) {
        super(domainType);
        this.dimensionName = dimensionName;
        this.supportedDimensionValueRange = supportedDimensionValueRange;
    }

    @Override
    public double[] getX(TwdTransition instance) {
        return new double[] { getDimensionValue(instance) };
    }

    /**
     * Gets pre-processed input value value from the provided instance which can be used for prediction and training
     * tasks with the regression model.
     */
    public double getDimensionValue(TwdTransition instance) {
        double dimensionValue = getDimensionValueUnpreprocessed(instance);
        return getPreprocessedDimensionValue(dimensionValue);
    }

    /**
     * Gets the input value from the provided instance in its original state without any pre-processing.
     */
    public abstract double getDimensionValueUnpreprocessed(TwdTransition instance);

    /**
     * Pre-processed the provided input value so that it can be used as input for prediction and training tasks with the
     * regression model.
     */
    public double getPreprocessedDimensionValue(double dimensionValue) {
        if (getSupportedDimensionValueRange().isSquareRootInput()) {
            return Math.sqrt(dimensionValue);
        }
        return dimensionValue;
    }

    /**
     * Gets the interval of the input values which are supported by this model context. Additionally, it contains the
     * configuration of the model, e.g. polynomial order.
     */
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

    /**
     * Checks whether the provided input value is supported by this model context for prediction tasks. For this, it
     * must be within the value range provided as {@link SupportedDimensionValueRange} to the constructor of this class.
     */
    public boolean isDimensionValueSupported(double dimensionValue) {
        if (supportedDimensionValueRange.getFromInclusive() <= dimensionValue
                && (supportedDimensionValueRange.getToExclusive() > dimensionValue
                        || dimensionValue >= SupportedDimensionValueRange.MAX_VALUE)) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the provided input value is supported by this model context for training tasks. For this, it must
     * be within the value range provided as {@link SupportedDimensionValueRange} to the constructor of this class. In
     * contrast to {@link #isDimensionValueSupported(double)}, the value will be also supported for the training tasks,
     * if it matches with {@link SupportedDimensionValueRange#getToExclusive()}.
     */
    public boolean isDimensionValueSupportedForTraining(double dimensionValue) {
        return isDimensionValueSupported(dimensionValue)
                || supportedDimensionValueRange.getToExclusive() == dimensionValue;
    }

    @Override
    public boolean isContainsAllFeatures(TwdTransition instance) {
        double x = getDimensionValueUnpreprocessed(instance);
        return isDimensionValueSupported(x);
    }

}
