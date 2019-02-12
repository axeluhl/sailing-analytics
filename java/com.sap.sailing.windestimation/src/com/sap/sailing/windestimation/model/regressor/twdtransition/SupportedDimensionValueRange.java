package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.io.Serializable;

public class SupportedDimensionValueRange implements Serializable {

    private static final long serialVersionUID = -3288594458162374160L;
    public static final double MAX_VALUE = 100000000000.0;
    public static final int SQUARE_ROOT_AS_POLYNOMIAL_DEGREE = -1;
    private final double fromInclusive;
    private final double toExclusive;
    private final int polynomialDegree;
    private final boolean withBias;
    private final boolean squareRootInput;

    public SupportedDimensionValueRange(double fromInclusive, double toExclusive, int polynomialDegree,
            boolean withBias) {
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
        this.polynomialDegree = Math.abs(polynomialDegree);
        this.withBias = withBias;
        this.squareRootInput = polynomialDegree == SQUARE_ROOT_AS_POLYNOMIAL_DEGREE;
    }

    public double getFromInclusive() {
        return fromInclusive;
    }

    public double getToExclusive() {
        return toExclusive;
    }

    public double getSupportedIntervalMiddleValue() {
        return fromInclusive + (toExclusive - fromInclusive) / 2;
    }

    public int getPolynomialDegree() {
        return polynomialDegree;
    }

    public int getPolynomialDegreeForRegressor() {
        return Math.abs(polynomialDegree);
    }

    public boolean isWithBias() {
        return withBias;
    }

    public boolean isSquareRootInput() {
        return squareRootInput;
    }

    @Override
    public String toString() {
        return "SupportedDimensionValueRange [fromInclusive=" + fromInclusive + ", toExclusive=" + toExclusive
                + ", polynomialDegree=" + polynomialDegree + ", withBias=" + withBias + ", squareRootInput="
                + squareRootInput + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(fromInclusive);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + polynomialDegree;
        result = prime * result + (squareRootInput ? 1231 : 1237);
        temp = Double.doubleToLongBits(toExclusive);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        SupportedDimensionValueRange other = (SupportedDimensionValueRange) obj;
        if (Double.doubleToLongBits(fromInclusive) != Double.doubleToLongBits(other.fromInclusive))
            return false;
        if (polynomialDegree != other.polynomialDegree)
            return false;
        if (squareRootInput != other.squareRootInput)
            return false;
        if (Double.doubleToLongBits(toExclusive) != Double.doubleToLongBits(other.toExclusive))
            return false;
        if (withBias != other.withBias)
            return false;
        return true;
    }

}
