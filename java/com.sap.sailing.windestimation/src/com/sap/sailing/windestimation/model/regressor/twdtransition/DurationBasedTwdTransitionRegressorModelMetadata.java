package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.store.PersistenceContextType;

public class DurationBasedTwdTransitionRegressorModelMetadata
        extends SingleDimensionBasedTwdTransitionRegressorModelMetadata {

    private static final String DIMENSION_NAME = "Duration";
    private static final long serialVersionUID = 4324543543l;

    public DurationBasedTwdTransitionRegressorModelMetadata(DurationValueRange durationValueRange) {
        super(DIMENSION_NAME, PersistenceContextType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR,
                durationValueRange.getSupportedDimensionValueRange());
    }

    @Override
    public double getDimensionValue(TwdTransition instance) {
        double seconds = instance.getDuration().asSeconds();
        return getPreprocessedDimensionValue(seconds);
    }

    @Override
    public double getPreprocessedDimensionValue(double seconds) {
        if (getSupportedDimensionValueRange().equals(DurationValueRange.MIDDLE1.getSupportedDimensionValueRange())) {
            seconds = Math.sqrt(seconds);
        }
        return seconds;
    }

    public enum DurationValueRange {
        BEGINNING(0, 5, 1, false), MIDDLE1(5, 62, 1, true), MIDDLE2(62, 5394, 1, true), REMAINDER(5394,
                Double.MAX_VALUE, 1, true);

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
