package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.io.Serializable;

/**
 * Represents an interval for which a certain regression model is responsible for. Additionally, it contains
 * configuration for the regression model, such as polynomial degree to use, whether a bias should be used, or not.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SupportedDimensionValueRange implements Serializable {

    private static final long serialVersionUID = -3288594458162374160L;
    public static final double MAX_VALUE = 100000000000.0;
    public static final int SQUARE_ROOT_AS_POLYNOMIAL_DEGREE = -1;
    private final double fromInclusive;
    private final double toExclusive;
    private final int polynomialDegree;
    private final boolean withBias;
    private final boolean squareRootInput;

    /**
     * Constructs a value range with configuration which is meant to be managed by a certain model.
     * 
     * @param fromInclusive
     *            Lowest value of the interval (inclusive)
     * @param toExclusive
     *            Highest value of the interval (exclusive). {@link #MAX_VALUE} represents positive infinity.
     * @param polynomialDegree
     *            Polynomial order of the regression to use for this interval. {@link #SQUARE_ROOT_AS_POLYNOMIAL_DEGREE}
     *            means that the polynomial order gets 1, but the input value must be square rooted before passing it to
     *            the model for prediction (see
     *            {@link SingleDimensionBasedTwdTransitionRegressorModelContext#getPreprocessedDimensionValue(double)}.
     *            This logic leads to a square root function being learned instead of a linear one.
     * @param withBias
     *            Whether the regression model shall contain a bias, e.g. {@code ax + b} where {@code b} is bias.
     */
    public SupportedDimensionValueRange(double fromInclusive, double toExclusive, int polynomialDegree,
            boolean withBias) {
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
        this.polynomialDegree = Math.abs(polynomialDegree);
        this.withBias = withBias;
        this.squareRootInput = polynomialDegree == SQUARE_ROOT_AS_POLYNOMIAL_DEGREE;
    }

    /**
     * Gets the lowest value of the represented interval (inclusive).
     */
    public double getFromInclusive() {
        return fromInclusive;
    }

    /**
     * Gets the highest value of the represented interval (exclusive).
     */
    public double getToExclusive() {
        return toExclusive;
    }

    /**
     * Gets the middle value between the interval boundaries.
     */
    public double getSupportedIntervalMiddleValue() {
        return fromInclusive + (toExclusive - fromInclusive) / 2;
    }

    /**
     * Gets the polynomial order requirement for a regression model. If this instance was constructed with polynomial
     * order set with {@link #SQUARE_ROOT_AS_POLYNOMIAL_DEGREE}, then 1 will be returned.
     */
    public int getPolynomialDegree() {
        return polynomialDegree;
    }

    /**
     * Checks whether the regression model shall contain a bias, e.g. {@code ax + b} where {@code b} is bias.
     */
    public boolean isWithBias() {
        return withBias;
    }

    /**
     * Checks whether the regression model requires its input being square rooted before passing it to the model for
     * prediction or training. This will be the case if this instance was constructed with polynomial order set with
     * {@link #SQUARE_ROOT_AS_POLYNOMIAL_DEGREE}.
     */
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
