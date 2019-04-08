package com.sap.sailing.polars.regression.impl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.MatrixVisitorException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularMatrixException;

import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * This class implements the incremental polynomial regression approach described here:
 * 
 * <a href=
 * "http://erikerlandson.github.io/blog/2012/07/05/deriving-an-incremental-form-of-the-polynomial-regression-equations/"
 * > http://erikerlandson.github.io/blog/2012/07/05/deriving-an-incremental-form-of-the-polynomial-regression-equations/
 * </a><p>
 * 
 * It can be used in parallel, because it uses locking for adding and reading data.
 * 
 * 
 * @author Frederik Petersen D054528
 *
 */
public class IncrementalAnyOrderLeastSquaresImpl implements IncrementalLeastSquares {

    private static final long serialVersionUID = -5282614133220702724L;

    private class NaNCheckerVisitor implements RealMatrixPreservingVisitor {

        private boolean hasNaNEntry = false;

        @Override
        public void visit(int arg0, int arg1, double arg2) throws MatrixVisitorException {
            if (Double.isNaN(arg2)) {
                hasNaNEntry = true;
            }
        }

        @Override
        public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        }

        @Override
        public double end() {
            return 0.0;
        }

        public boolean hasNaNEntry() {
            return hasNaNEntry;
        }

    }

    private double[][] matrixOfXSums;

    private double[] vectorOfXYMultSums;

    private int polynomialOrder;

    private AtomicLong numberOfPointsAdded = new AtomicLong(0);

    private AtomicBoolean functionNeedsUpdate = new AtomicBoolean(true);

    private transient NamedReentrantReadWriteLock lock = new NamedReentrantReadWriteLock("IncrementalLeastSquaresLock",
            false);

    private transient NamedReentrantReadWriteLock cacheLock = new NamedReentrantReadWriteLock(
            "IncrementalLeastSquaresCacheLock", true);

    private PolynomialFunction cachedFunction;

    private final boolean hasIntercept;
    private final boolean useSymbollicInversionIfPossible;
    
    public IncrementalAnyOrderLeastSquaresImpl(double[][] matrixOfXSums, double[] vectorOfXYMultSums, int polynomialOrder, boolean hasIntercept,
            boolean useSymbollicInversionIfPossible, long numberOfPointsAdded) {
        this.hasIntercept = hasIntercept;
        this.polynomialOrder = polynomialOrder;
        this.useSymbollicInversionIfPossible = useSymbollicInversionIfPossible;
        this.matrixOfXSums = matrixOfXSums;
        this.vectorOfXYMultSums = vectorOfXYMultSums;
        this.numberOfPointsAdded.set(numberOfPointsAdded);
        functionNeedsUpdate.set(true);
    }

    public IncrementalAnyOrderLeastSquaresImpl(int polynomialOrder, boolean hasIntercept,
            boolean useSymbollicInversionIfPossible) {
        this.hasIntercept = hasIntercept;
        this.polynomialOrder = polynomialOrder;
        this.useSymbollicInversionIfPossible = useSymbollicInversionIfPossible;
        if (hasIntercept) {
            matrixOfXSums = new double[polynomialOrder + 1][polynomialOrder + 1];
            vectorOfXYMultSums = new double[polynomialOrder + 1];
        } else {
            matrixOfXSums = new double[polynomialOrder][polynomialOrder];
            vectorOfXYMultSums = new double[polynomialOrder];
        }
    }

    public IncrementalAnyOrderLeastSquaresImpl(int polynomialOrder) {
        this(polynomialOrder, true, true);
    }

    public IncrementalAnyOrderLeastSquaresImpl(int polynomialOrder, boolean hasIntercept) {
        this(polynomialOrder, hasIntercept, true);
    }

    @Override
    public void addData(double x, double y) {
        LockUtil.lockForWrite(lock);
        try {
            numberOfPointsAdded.incrementAndGet();
            functionNeedsUpdate.set(true);
            for (int i = 0; hasIntercept ? i <= polynomialOrder : i < polynomialOrder; i++) {
                int powerI = hasIntercept ? i : i + 1;
                vectorOfXYMultSums[i] += y * Math.pow(x, powerI);
                for (int j = 0; hasIntercept ? j <= polynomialOrder : j < polynomialOrder; j++) {
                    int powerJ = hasIntercept ? j : j + 1;
                    if (powerI == 0 && powerJ == 0) {
                        matrixOfXSums[i][j] += 1;
                    } else {
                        matrixOfXSums[i][j] += Math.pow(x, powerI + powerJ);
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

    private PolynomialFunction createEstimatingPolynomialFunction() throws NotEnoughDataHasBeenAddedException {
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
        LeastSquaresInversionHelperType type = LeastSquaresInversionHelperType.getType(polynomialOrder, hasIntercept);
        RealMatrix inversedMatrix = null;
        if (useSymbollicInversionIfPossible && type != null) {
            inversedMatrix = type.inverseMatrix(matrixOfXSumsCopy);
        }
        if (inversedMatrix == null) {
            try {
                inversedMatrix = new LUDecompositionImpl(matrixOfXSumsCopy).getSolver().getInverse();
            } catch (SingularMatrixException singularMatrixException) {
                throw new NotEnoughDataHasBeenAddedException("Matrix singular, all x input equal?",
                        singularMatrixException);
            }
        } else {
            NaNCheckerVisitor nanChecker = new NaNCheckerVisitor();
            inversedMatrix.walkInOptimizedOrder(nanChecker);
            if (nanChecker.hasNaNEntry()) {
                throw new NotEnoughDataHasBeenAddedException("Matrix singular, all x input equal?");
            }

        }
        RealVector coeffs = inversedMatrix.operate(vectorOfXYMultSumsCopy);
        if (!hasIntercept) {
            resultFunction = createFunctionWithZeroIntercept(coeffs);
        } else {
            resultFunction = new PolynomialFunction(coeffs.toArray());
        }
        return resultFunction;
    }

    private PolynomialFunction createFunctionWithZeroIntercept(RealVector coeffs) {
        PolynomialFunction resultFunction;
        double[] coeffsNoIntercept = coeffs.toArray();
        double[] coeffsWithZeroIntercept = new double[coeffsNoIntercept.length + 1];
        coeffsWithZeroIntercept[0] = 0;
        for (int i = 1; i < coeffsWithZeroIntercept.length; i++) {
            coeffsWithZeroIntercept[i] = coeffsNoIntercept[i - 1];
        }
        resultFunction = new PolynomialFunction(coeffsWithZeroIntercept);
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

    private enum LeastSquaresInversionHelperType {

        CUBIC, CUBIC_NO_INTERCEPT;

        /**
         * Use to provide a fast alternative to actually computing the inverseMatrix from scratch.
         * Right now this uses precalculated equations for cubic functions.
         * 
         * @param realMatrix
         *            matrix to inverse
         * @return Inversed matrix if symbolic inversion was found. Else null
         */
        private RealMatrix inverseMatrix(RealMatrix realMatrix) {
            RealMatrix result = null;
            switch (this) {
            case CUBIC_NO_INTERCEPT:
                double[][] originDataCNI = realMatrix.getData();
                double a = originDataCNI[0][0];
                double b = originDataCNI[0][1];
                double c = originDataCNI[0][2];
                double d = originDataCNI[1][2];
                double e = originDataCNI[2][2];
                double[][] matrixDataCNI = { { d * d - c * e, b * e - c * d, c * c - b * d },
                        { b * e - c * d, c * c - a * e, a * d - b * c },
                        { c * c - b * d, a * d - b * c, b * b - a * c } };
                RealMatrix intermediateMatrixCNI = MatrixUtils.createRealMatrix(matrixDataCNI);
                double factorCNI = (1.0 / (-a * c * e + a * d * d + b * b * e - 2 * b * c * d + c * c * c));
                result = intermediateMatrixCNI.scalarMultiply(factorCNI);
                break;
            case CUBIC:
                double[][] originDataC = realMatrix.getData();
                double n = originDataC[0][0];
                a = originDataC[0][1];
                b = originDataC[0][2];
                c = originDataC[0][3];
                d = originDataC[1][3];
                e = originDataC[2][3];
                double f = originDataC[3][3];
                double[][] matrixDataC = {
                        { -d * d * d + 2 * c * e * d + b * f * d - b * e * e - c * c * f,
                                -e * c * c + d * d * c + b * f * c + a * e * e - b * d * e - a * d * f,
                                -f * b * b + d * d * b + c * e * b - c * c * d - a * d * e + a * c * f,
                                c * c * c - 2 * b * d * c - a * e * c + a * d * d + b * b * e },
                        { -e * c * c + d * d * c + b * f * c + a * e * e - b * d * e - a * d * f,
                                -f * b * b + 2 * c * e * b - c * c * d - e * e * n + d * f * n,
                                c * c * c - b * d * c - a * e * c - f * n * c + a * b * f + d * e * n,
                                d * b * b - c * c * b - a * e * b + a * c * d - d * d * n + c * e * n },
                        { -f * b * b + d * d * b + c * e * b - c * c * d - a * d * e + a * c * f,
                                c * c * c - b * d * c - a * e * c - f * n * c + a * b * f + d * e * n,
                                -f * a * a + 2 * c * d * a - b * c * c - d * d * n + b * f * n,
                                e * a * a - c * c * a - b * d * a + b * b * c + c * d * n - b * e * n },
                        { c * c * c - 2 * b * d * c - a * e * c + a * d * d + b * b * e,
                                d * b * b - c * c * b - a * e * b + a * c * d - d * d * n + c * e * n,
                                e * a * a - c * c * a - b * d * a + b * b * c + c * d * n - b * e * n,
                                -b * b * b + 2 * a * c * b + d * n * b - a * a * d - c * c * n } };
                RealMatrix intermediateMatrixC = MatrixUtils.createRealMatrix(matrixDataC);
                double factorC = 1.0 / (-a * a * d * f + a * a * e * e + 2 * c
                        * (a * (b * f + d * d) + e * (b * b + d * n)) - c * c * (2 * a * e + 3 * b * d + f * n) + b
                        * (-2 * a * d * e + d * f * n + e * e * -n) - b * b * b * f + b * b * d * d + c * c * c * c - d
                        * d * d * n);
                result = intermediateMatrixC.scalarMultiply(factorC);
                break;
            default:
                break;
            }
            return result;
        }

        /**
         * 
         * @param order
         *            Order of the polynomial
         * @param hasIntercept
         *            If the least squares is set to no intercept
         * @return type of the polynomial or null if it doesn't exist in this implementation
         */
        private static LeastSquaresInversionHelperType getType(int order, boolean hasIntercept) {
            LeastSquaresInversionHelperType result = null;
            switch (order) {
            case 3:
                result = hasIntercept ? CUBIC : CUBIC_NO_INTERCEPT;
                break;
            }
            return result;
        }

    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        lock = new NamedReentrantReadWriteLock("IncrementalLeastSquaresLock", false);
        cacheLock = new NamedReentrantReadWriteLock("IncrementalLeastSquaresCacheLock", true);
    }

    @Override
    public long getNumberOfAddedPoints() {
        return numberOfPointsAdded.get();
    }

    public double[][] getMatrixOfXSums() {
        return matrixOfXSums;
    }

    public double[] getVectorOfXYMultSums() {
        return vectorOfXYMultSums;
    }

    public int getPolynomialOrder() {
        return polynomialOrder;
    }

    public boolean isHasIntercept() {
        return hasIntercept;
    }

    public boolean isUseSymbollicInversionIfPossible() {
        return useSymbollicInversionIfPossible;
    }

}
