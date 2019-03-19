package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.store.ModelDomainType;

/**
 * Model context for duration-based standard deviation of TWD delta regression models. The context is defined by the
 * intervals of seconds where each interval gets managed by an individual model.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public final class DurationBasedTwdTransitionRegressorModelContext
        extends SingleDimensionBasedTwdTransitionRegressorModelContext {

    private static final String DIMENSION_NAME = "Duration";
    private static final long serialVersionUID = 4324543543l;

    /**
     * Constructs a new model context for the provided value interval.
     * 
     * @param durationValueRange
     *            The value interval which is represented by this context
     */
    public DurationBasedTwdTransitionRegressorModelContext(DurationValueRange durationValueRange) {
        super(DIMENSION_NAME, ModelDomainType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR,
                durationValueRange.getSupportedDimensionValueRange());
    }

    @Override
    public double getDimensionValueUnpreprocessed(TwdTransition instance) {
        double seconds = instance.getDuration().asSeconds();
        return seconds;
    }

    /**
     * Input value intervals with corresponding model configurations for the duration dimension which is treated in
     * seconds. For each enum element, a separate model will be trained with polynomial degree and bias as specified by
     * the enum element. Feel free to add/delete/modify enum elements as it is desired. The model training and discovery
     * will still work. However, make sure that the specified intervals do not include holes between its transitions.
     * 
     * @author Vladislav Chumak (D069712)
     *
     */
    public enum DurationValueRange {
        BEGINNING(0, 1, 1, false),
        MIDDLE1(1, 140, SupportedDimensionValueRange.SQUARE_ROOT_AS_POLYNOMIAL_DEGREE, true),
        MIDDLE2(140, 5394, 1, true),
        REMAINDER(5394, SupportedDimensionValueRange.MAX_VALUE, 1, true);

        private final SupportedDimensionValueRange supportedDimensionValueRange;

        /**
         * Constructs a value range with configuration which is meant to be managed by a separate model.
         * 
         * @param fromInclusive
         *            Lowest value of the interval (inclusive)
         * @param toExclusive
         *            Highest value of the interval (exclusive). {@link SupportedDimensionValueRange#MAX_VALUE}
         *            represents positive infinity.
         * @param polynomialDegree
         *            Polynomial order of the regression to use for this interval.
         *            {@link SupportedDimensionValueRange#SQUARE_ROOT_AS_POLYNOMIAL_DEGREE} means that the polynomial
         *            order gets 1, but the input value must be square rooted before passing it to the model for
         *            prediction (see
         *            {@link SingleDimensionBasedTwdTransitionRegressorModelContext#getPreprocessedDimensionValue(double)}.
         *            This logic leads to a square root function being learned instead of a linear one.
         * @param withBias
         *            Whether the regression model shall contain a bias, e.g. {@code ax + b} where {@code b} is bias.
         */
        private DurationValueRange(double fromInclusive, double toExclusive, int polynomialDegree, boolean withBias) {
            this.supportedDimensionValueRange = new SupportedDimensionValueRange(fromInclusive, toExclusive,
                    polynomialDegree, withBias);
        }

        /**
         * Gets the value range/interval represented by the enum element with its corresponding model configuration.
         */
        public SupportedDimensionValueRange getSupportedDimensionValueRange() {
            return supportedDimensionValueRange;
        }

    }

}
