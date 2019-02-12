package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.store.ModelDomainType;

public final class DurationBasedTwdTransitionRegressorModelContext
        extends SingleDimensionBasedTwdTransitionRegressorModelContext {

    private static final String DIMENSION_NAME = "Duration";
    private static final long serialVersionUID = 4324543543l;

    public DurationBasedTwdTransitionRegressorModelContext(DurationValueRange durationValueRange) {
        super(DIMENSION_NAME, ModelDomainType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR,
                durationValueRange.getSupportedDimensionValueRange());
    }

    @Override
    public double getDimensionValue(TwdTransition instance) {
        double seconds = instance.getDuration().asSeconds();
        return getPreprocessedDimensionValue(seconds);
    }

    public enum DurationValueRange {
        BEGINNING(0, 5, 1, false), MIDDLE1(5, 62, SupportedDimensionValueRange.SQUARE_ROOT_AS_POLYNOMIAL_DEGREE,
                true), MIDDLE2(62, 5394, 1, true), REMAINDER(5394, SupportedDimensionValueRange.MAX_VALUE, 1, true);

        private final SupportedDimensionValueRange supportedDimensionValueRange;

        private DurationValueRange(double fromInclusive, double toExclusive, int polynomialDegree, boolean withBias) {
            this.supportedDimensionValueRange = new SupportedDimensionValueRange(fromInclusive, toExclusive,
                    polynomialDegree, withBias);
        }

        public SupportedDimensionValueRange getSupportedDimensionValueRange() {
            return supportedDimensionValueRange;
        }

    }

}
