package com.sap.sailing.polars.regression;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;

/**
 * Support incremental regression linear in one variable but potentially of higher order.
 * 
 * @author Frederik Petersen (D054528)
 *
 */
public interface IncrementalLeastSquares {

    /**
     * Adds data to the least squares regression. This only updates the internal matrix and vector and will not trigger the 
     * actual least square solving. See {@link #getOrCreatePolynomialFunction()}.
     * @param x
     * @param y
     */
    public abstract void addData(double x, double y);

    /**
     * The polynomial function is cached and will be returned immediately if no data has been added since the last call.
     * Otherwise the calculation is performed and the polynomial function is returned. It has a complexity of O(order) where
     * order is the requested order of the polynomial.
     * 
     * @return polynomial function estimating the data. Can be constant when only one data point has been added.
     * @throws NotEnoughDataHasBeenAddedException Will be returned if no data has been added to the regression.
     */
    public abstract PolynomialFunction getOrCreatePolynomialFunction() throws NotEnoughDataHasBeenAddedException;

    public abstract long getNumberOfAddedPoints();

}