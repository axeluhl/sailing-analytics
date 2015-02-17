package com.sap.sailing.polars.regression.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * This class implements the incremental polynomial regression approach described here:
 * 
 * <a href="http://erikerlandson.github.io/blog/2012/07/05/deriving-an-incremental-form-of-the-polynomial-regression-equations/">
 * http://erikerlandson.github.io/blog/2012/07/05/deriving-an-incremental-form-of-the-polynomial-regression-equations/</a>
 * 
 * 
 * @author Frederik Petersen D054528
 *
 */
public class IncrementalAnyOrderLeastSquaresImpl implements IncrementalLeastSquares {

    private double[][] matrixOfXSums;

    private double[] vectorOfXYMultSums;

    private int polynomialOrder;
    
    private AtomicLong numberOfPointsAdded = new AtomicLong(0);
    
    private AtomicBoolean functionNeedsUpdate = new AtomicBoolean(true);

    private final NamedReentrantReadWriteLock lock = new NamedReentrantReadWriteLock("IncrementalLeastSquaresLock",
            false);
    
    private final NamedReentrantReadWriteLock cacheLock = new NamedReentrantReadWriteLock("IncrementalLeastSquaresCacheLock",
            true);

    private PolynomialFunction cachedFunction;
    
    public IncrementalAnyOrderLeastSquaresImpl(int polynomialOrder) {
        this.polynomialOrder = polynomialOrder;
        matrixOfXSums = new double[polynomialOrder + 1][polynomialOrder + 1];
        vectorOfXYMultSums = new double[polynomialOrder + 1];
    }
    
    @Override
    public void addData(double x, double y) {
        LockUtil.lockForWrite(lock);
        try {
            numberOfPointsAdded.incrementAndGet();
            functionNeedsUpdate.set(true);
            for (int i = 0; i <= polynomialOrder; i++) {
                vectorOfXYMultSums[i] += y * Math.pow(x, i);
                for (int j = 0; j <= polynomialOrder; j++) {
                    if (i == 0 && j == 0) {
                        matrixOfXSums[i][j] += 1;
                    } else {
                        matrixOfXSums[i][j] += Math.pow(x, i+j);
                    }
                }
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }
    
    @Override
    public PolynomialFunction getOrCreatePolynomialFunction() throws NotEnoughDataHasBeenAddedException {

        PolynomialFunction resultFunction;
        if (!functionNeedsUpdate.get()) {
            LockUtil.lockForRead(cacheLock);
            try {
                resultFunction = new PolynomialFunction(cachedFunction.getCoefficients());
            } finally {
                LockUtil.unlockAfterRead(cacheLock);
            }
        } else {
            if (numberOfPointsAdded.get() < 1) {
                throw new NotEnoughDataHasBeenAddedException("No points have been added to the regression yet.");
            }
            if (numberOfPointsAdded.get() == 1) {
                resultFunction = createConstantFunctionForOneDataPoint();
            } else {
                resultFunction = createEstimatingPolynomialFunction();
            }
            LockUtil.lockForWrite(cacheLock);
            try {
                cachedFunction = new PolynomialFunction(resultFunction.getCoefficients());
            } finally {
                LockUtil.unlockAfterWrite(cacheLock);
            }
            functionNeedsUpdate.set(false);
        }
        return resultFunction;
    }

    private PolynomialFunction createEstimatingPolynomialFunction() {
        PolynomialFunction resultFunction;
        RealMatrix matrixOfXSumsCopy;
        RealVector vectorOfXYMultSumsCopy;
        LockUtil.lockForRead(lock);
        try {
            matrixOfXSumsCopy = MatrixUtils.createRealMatrix(matrixOfXSums);
            vectorOfXYMultSumsCopy = MatrixUtils.createRealVector(vectorOfXYMultSums);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
        RealMatrix inversedMatrix = new LUDecompositionImpl(matrixOfXSumsCopy).getSolver().getInverse();
        RealVector coeffs = inversedMatrix.operate(vectorOfXYMultSumsCopy);
        resultFunction = new PolynomialFunction(coeffs.toArray());
        return resultFunction;
    }

    private PolynomialFunction createConstantFunctionForOneDataPoint() {
        LockUtil.lockForRead(lock);
        double[] coeff = new double[1];
        try {
            coeff[0] = vectorOfXYMultSums[0];
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
        return new PolynomialFunction(coeff);
    }

}
