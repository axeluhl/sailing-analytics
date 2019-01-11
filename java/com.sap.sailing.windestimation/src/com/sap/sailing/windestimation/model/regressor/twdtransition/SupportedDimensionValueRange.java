package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.io.Serializable;

public class SupportedDimensionValueRange implements Serializable {

    private static final long serialVersionUID = -3288594458162374160L;
    private final double fromInclusive;
    private final double toExclusive;
    private int polynomialDegree;
    private boolean withBias;

    public SupportedDimensionValueRange(double fromInclusive, double toExclusive, int polynomialDegree,
            boolean withBias) {
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

    public double getSupportedIntervalMiddleValue() {
        return fromInclusive + (toExclusive - fromInclusive) / 2;
    }

    public int getPolynomialDegree() {
        return polynomialDegree;
    }

    public boolean isWithBias() {
        return withBias;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(fromInclusive);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + polynomialDegree;
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
        if (Double.doubleToLongBits(toExclusive) != Double.doubleToLongBits(other.toExclusive))
            return false;
        if (withBias != other.withBias)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SupportedDimensionValueRange [fromInclusive=" + fromInclusive + ", toExclusive=" + toExclusive
                + ", polynomialDegree=" + polynomialDegree + ", withBias=" + withBias + "]";
    }

}
