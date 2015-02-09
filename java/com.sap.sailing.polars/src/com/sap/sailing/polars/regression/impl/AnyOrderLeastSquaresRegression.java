package com.sap.sailing.polars.regression.impl;

import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * Thread safe implementation of the Least Squares regression for any order of result polynomial.
 * 
 * {@link #addData(double, double)} will incrementally update the simultaneous equations needed for the calculation of
 * the result coefficients.
 * 
 * {@link #getCoefficiants()} will do the solving of the equations, if new data has been added since the last
 * calculation.
 * 
 * @author D054528
 *
 */
public class AnyOrderLeastSquaresRegression {
    
    private final double aMatrix[][];
    private final double bMatrix[];
    private final double term[];
    private final int nOrder;
    
    private boolean needsCoeffUpdate = true;
    private double[] coeffs;
    
    private final NamedReentrantReadWriteLock lock = new NamedReentrantReadWriteLock("LeastSquaresLock", true);

    /**
     * 
     * @param nOrder order of the polynomial you want to fit over the data.
     */
    public AnyOrderLeastSquaresRegression(int nOrder) {
        this.nOrder = nOrder;
        aMatrix = new double[nOrder + 1][nOrder + 1];
        bMatrix = new double[nOrder + 1];
        term = new double[nOrder + 1];
    }
    
    /**
     * Add data to the regression. This will NOT trigger a calculation of the coefficiants, but only update the matrices
     * that form the simultaneous equations. 
     * 
     * Use {@link #getCoefficiants()} to retrieve the coefficiants of the fit.
     * 
     * @param yValue
     * @param xValue
     */
    public void addData(double yValue, double xValue) {
        LockUtil.lockForWrite(lock);
        try {
            needsCoeffUpdate = true;
            // sum the y values
            bMatrix[0] += yValue;
            // sum the x power values
            double xpower = 1;
            for (int j = 0; j < nOrder + 1; j++) {
                term[j] = xpower;
                aMatrix[0][j] += xpower;
                xpower = xpower * xValue;
            }
            // now set up the rest of rows in the matrix - multiplying each row by each term
            for (int j = 1; j < nOrder + 1; j++) {
                bMatrix[j] += yValue * term[j];
                for (int k = 0; k < bMatrix.length; k++) {
                    aMatrix[j][k] += term[j] * term[k];
                }
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }
    
    /**
     * If the coefficiants have been requested before, and not data was added, the coefficiants will simply be returned.
     * If data was added since the last time this method was called, the calculation will take place.
     * 
     * The calculation is standard gaussian solving of the simultaneous equations. It has the complexity (nOrder + 1)^3
     * 
     * @return Coefficiants a[] of the polynomial function. Sorted by power => a[0]*x^0 to a[n]*x^n.
     */
    public double[] getCoefficiants() {
        if (needsCoeffUpdate) {
            double[] coeffs = gauss();
            LockUtil.lockForWrite(lock);
            try {
                this.coeffs = coeffs;
                needsCoeffUpdate = false;
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        }
        LockUtil.lockForRead(lock);
        double[] result;
        try {
            result = this.coeffs;
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
        return result;
    }

    /**
     * a standard gaussian technique for / solving simultaneous equations of the form: |A| = |B| * |C| where we / know
     * the values of |A| and |B|, and we are solving for the coefficients / in |C|
     */
    private double[] gauss() {
        double aCopy[][] = new double[aMatrix.length][aMatrix[0].length];
        double bCopy[] = new double[bMatrix.length];
        double pivot;
        double mult;
        double top;
        int n = bCopy.length;
        double coef[] = new double[n];

        // copy over the array values - inplace solution changes values
        LockUtil.lockForRead(lock);
        try {
            for (int i = 0; i < aMatrix.length; i++) {
                for (int j = 0; j < aMatrix[i].length; j++) {
                    aCopy[i][j] = aMatrix[i][j];
                }
                bCopy[i] = bMatrix[i];
            }
        } finally {
            LockUtil.unlockAfterRead(lock);
        }

        for (int j = 0; j < (n - 1); j++) {
            pivot = aCopy[j][j];
            for (int i = j + 1; i < n; i++) {
                mult = aCopy[i][j] / pivot;
                for (int k = j + 1; k < n; k++) {
                    aCopy[i][k] = aCopy[i][k] - mult * aCopy[j][k];
                }
                bCopy[i] = bCopy[i] - mult * bCopy[j];
            }
        }

        coef[n - 1] = bCopy[n - 1] / aCopy[n - 1][n - 1];
        for (int i = n - 2; i >= 0; i--) {
            top = bCopy[i];
            for (int k = i + 1; k < n; k++) {
                top = top - aCopy[i][k] * coef[k];
            }
            coef[i] = top / aCopy[i][i];
        }
        return coef;
    }

}
