package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;

public class DistanceBasedTwdTransitionRegressorModelMetadata
        extends SingleDimensionBasedTwdTransitionRegressorModelMetadata {

    private static final String DIMENSION_NAME = "Distance";
    private static final long serialVersionUID = 4324543543l;
    private final DistanceValueRange distanceValueRange;

    public DistanceBasedTwdTransitionRegressorModelMetadata(DistanceValueRange distanceValueRange) {
        super(DIMENSION_NAME, distanceValueRange.getPolynomialDegree(), distanceValueRange.isWithBias());
        this.distanceValueRange = distanceValueRange;
    }

    @Override
    public double getDimensionValue(TwdTransition instance) {
        return instance.getDistance().getMeters();
    }

    @Override
    protected String getSupportedDimensionValueRangeId() {
        return "From" + distanceValueRange.getFromInclusive() + "To" + distanceValueRange.getToExclusive();
    }

    public enum DistanceValueRange {
        BEGINNING(0, 10, 2, false), REMAINDER(10, Double.MAX_VALUE, 1, true);

        private final double fromInclusive;
        private final double toExclusive;
        private int polynomialDegree;
        private boolean withBias;

        private DistanceValueRange(double fromInclusive, double toExclusive, int polynomialDegree, boolean withBias) {
            this.fromInclusive = fromInclusive;
            this.toExclusive = toExclusive;
            this.polynomialDegree = polynomialDegree;
            this.withBias = withBias;
        }

        public double getFromInclusive() {
            return fromInclusive;
        }

        public double getToExclusive() {
            return toExclusive;
        }

        public int getPolynomialDegree() {
            return polynomialDegree;
        }

        public boolean isWithBias() {
            return withBias;
        }

        public boolean isWithinRange(double x) {
            if (fromInclusive <= x && (toExclusive > x || toExclusive == Double.MAX_VALUE)) {
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean isDimensionValueSupported(double dimensionValue) {
        return distanceValueRange.isWithinRange(dimensionValue);
    }

}
