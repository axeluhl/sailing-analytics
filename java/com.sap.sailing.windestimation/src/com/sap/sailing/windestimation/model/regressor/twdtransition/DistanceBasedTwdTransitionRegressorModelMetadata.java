package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.store.PersistenceContextType;

public class DistanceBasedTwdTransitionRegressorModelMetadata
        extends SingleDimensionBasedTwdTransitionRegressorModelMetadata {

    private static final String DIMENSION_NAME = "Distance";
    private static final long serialVersionUID = 4324543543l;

    public DistanceBasedTwdTransitionRegressorModelMetadata(DistanceValueRange distanceValueRange) {
        super(DIMENSION_NAME, PersistenceContextType.DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR,
                distanceValueRange.getSupportedDimensionValueRange());
    }

    @Override
    public double getDimensionValue(TwdTransition instance) {
        double meters = instance.getDistance().getMeters();
        return getPreprocessedDimensionValue(meters);
    }

    @Override
    public double getPreprocessedDimensionValue(double meters) {
        return meters;
    }

    public enum DistanceValueRange {
        BEGINNING(0, 80, 1, false), MIDDLE(80, 1368, 1, true), REMAINDER(1368, SupportedDimensionValueRange.MAX_VALUE,
                1, true);

        private final SupportedDimensionValueRange supportedDimensionValueRange;

        private DistanceValueRange(double fromInclusive, double toExclusive, int polynomialDegree, boolean withBias) {
            this.supportedDimensionValueRange = new SupportedDimensionValueRange(fromInclusive, toExclusive,
                    polynomialDegree, withBias);
        }

        public SupportedDimensionValueRange getSupportedDimensionValueRange() {
            return supportedDimensionValueRange;
        }

    }

}
