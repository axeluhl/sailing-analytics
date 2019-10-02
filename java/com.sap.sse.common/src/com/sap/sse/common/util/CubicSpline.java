package com.sap.sse.common.util;

import java.util.Arrays;
import java.util.function.Supplier;

import com.sap.sse.common.Util;

// <copyright file="CubicSpline.cs" company="Math.NET">
// Math.NET Numerics, part of the Math.NET Project
// http://numerics.mathdotnet.com
// http://github.com/mathnet/mathnet-numerics
//
// Copyright (c) 2009-2014 Math.NET
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
// </copyright>

/**
 * Cubic Spline Interpolation. Supports both differentiation and integration.
 * <p>
 * Based on: <a href="github.com">https://github.com/mathnet/mathnet-numerics/blob/master/src/Numerics/Interpolation/CubicSpline.cs</a>
 */
public class CubicSpline {
    /**
     * Left and right boundary conditions.
     */
    public static enum SplineBoundaryCondition {
        /**
         * Natural Boundary (Zero second derivative).
         */
        Natural,

        /**
         * Parabolically Terminated boundary.
         */
        ParabolicallyTerminated,

        /**
         * Fixed first derivative at the boundary.
         */
        FirstDerivative,

        /**
         * Fixed second derivative at the boundary.
         */
        SecondDerivative
    }
    
    private final double[] _x;
    private final double[] _c0;
    private final double[] _c1;
    private final double[] _c2;
    private final double[] _c3;
    private static final double EPSILON = 0.000000000001;
    
    Supplier<double[]>_indefiniteIntegral;
    
    private static boolean almostEqual(double d1, double d2) {
        return almostEqual(d1, d2, EPSILON);
    }

    private static boolean almostEqual(double d1, double d2, double epsilon) {
        return Math.abs(d1-d2)<=epsilon;
    }

    /**
     * @param x
     *            sample points (N+1), sorted ascending
     * @param c0
     *            Zero order spline coefficients (N)
     * @param c1
     *            First order spline coefficients (N)
     * @param c2
     *            second order spline coefficients (N)
     * @param c3
     *            third order spline coefficients (N)
     */
    public CubicSpline(double[] x, double[] c0, double[] c1, double[] c2, double[] c3) {
        if (x.length != c0.length + 1 || x.length != c1.length + 1 || x.length != c2.length + 1
                || x.length != c3.length + 1) {
            throw new IllegalArgumentException("Argument vectors must have same length");
        }
        if (x.length < 2) {
            throw new IllegalArgumentException("Array too small, must be at least two but was "+x.length);
        }
        _x = x;
        _c0 = c0;
        _c1 = c1;
        _c2 = c2;
        _c3 = c3;
        _indefiniteIntegral = this::computeIndefiniteIntegral;
    }

    /**
     * Create a Hermite cubic spline interpolation from a set of (x,y) value pairs and their slope (first derivative),
     * sorted ascendingly by x.
     */
    public static CubicSpline interpolateHermiteSorted(double[] x, double[] y, double[] firstDerivatives) {
        if (x.length != y.length || x.length != firstDerivatives.length) {
            throw new IllegalArgumentException("Argument vectors must have same length");
        }
        if (x.length < 2) {
            throw new IllegalArgumentException("Array too small, must be at least two but was "+x.length);
        }
        final double[] c0 = new double[x.length - 1];
        final double[] c1 = new double[x.length - 1];
        final double[] c2 = new double[x.length - 1];
        final double[] c3 = new double[x.length - 1];
        for (int i = 0; i < c1.length; i++) {
            double w = x[i + 1] - x[i];
            double w2 = w * w;
            c0[i] = y[i];
            c1[i] = firstDerivatives[i];
            c2[i] = (3 * (y[i + 1] - y[i]) / w - 2 * firstDerivatives[i] - firstDerivatives[i + 1]) / w;
            c3[i] = (2 * (y[i] - y[i + 1]) / w + firstDerivatives[i] + firstDerivatives[i + 1]) / w2;
        }
        return new CubicSpline(x, c0, c1, c2, c3);
    }

    /**
     * Create a Hermite cubic spline interpolation from an unsorted set of (x,y) value pairs and their slope (first
     * derivative). WARNING: Works in-place and can thus causes the data array to be reordered.
     */
    public static CubicSpline InterpolateHermiteInplace(double[] x, double[] y, double[] firstDerivatives) {
        if (x.length != y.length || x.length != firstDerivatives.length) {
            throw new IllegalArgumentException("Argument vectors must have same length");
        }
        if (x.length < 2) {
            throw new IllegalArgumentException("Array too small, must be at least two but was "+x.length);
        }
        Util.sort(x, y, firstDerivatives);
        return interpolateHermiteSorted(x, y, firstDerivatives);
    }

    /**
     * Create an Akima cubic spline interpolation from a set of (x,y) value pairs, sorted ascendingly by x. Akima
     * splines are robust to outliers.
     */
    public static CubicSpline interpolateAkimaSorted(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Argument vectors must have same length");
        }
        if (x.length < 5) {
            throw new IllegalArgumentException("Array too small, must be at least five but was "+x.length);
        }
        /* Prepare divided differences (diff) and weights (w) */
        final double[] diff = new double[x.length - 1];
        final double[] weights = new double[x.length - 1];
        for (int i = 0; i < diff.length; i++) {
            diff[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]);
        }
        for (int i = 1; i < weights.length; i++) {
            weights[i] = Math.abs(diff[i] - diff[i - 1]);
        }
        /* Prepare Hermite interpolation scheme */
        final double[] dd = new double[x.length];
        for (int i = 2; i < dd.length - 2; i++) {
            dd[i] = almostEqual(weights[i - 1], 0.0) && almostEqual(weights[i + 1], 0.0)
                    ? (((x[i + 1] - x[i]) * diff[i - 1]) + ((x[i] - x[i - 1]) * diff[i])) / (x[i + 1] - x[i - 1])
                    : ((weights[i + 1] * diff[i - 1]) + (weights[i - 1] * diff[i])) / (weights[i + 1] + weights[i - 1]);
        }
        dd[0] = differentiateThreePoint(x, y, 0, 0, 1, 2);
        dd[1] = differentiateThreePoint(x, y, 1, 0, 1, 2);
        dd[x.length - 2] = differentiateThreePoint(x, y, x.length - 2, x.length - 3, x.length - 2, x.length - 1);
        dd[x.length - 1] = differentiateThreePoint(x, y, x.length - 1, x.length - 3, x.length - 2, x.length - 1);
        /* Build Akima spline using Hermite interpolation scheme */
        return interpolateHermiteSorted(x, y, dd);
    }

    /**
     * Create an Akima cubic spline interpolation from an unsorted set of (x,y) value pairs. Akima splines are robust to
     * outliers. WARNING: Works in-place and can thus causes the data array to be reordered.
     */
    public static CubicSpline interpolateAkimaInplace(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Argument vectors must have same length");
        }
        Util.sort(x, y);
        return interpolateAkimaSorted(x, y);
    }

    /**
     * Create a cubic spline interpolation from a set of (x,y) value pairs, sorted ascendingly by x, and custom
     * boundary/termination conditions.
     */
    public static CubicSpline interpolateBoundariesSorted(double[] x, double[] y,
            SplineBoundaryCondition leftBoundaryCondition, double leftBoundary,
            SplineBoundaryCondition rightBoundaryCondition, double rightBoundary) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Argument vectors must have same length");
        }
        if (x.length < 2) {
            throw new IllegalArgumentException("Array too small, must be at least two but was "+x.length);
        }
        int n = x.length;
        // normalize special cases
        if ((n == 2) && (leftBoundaryCondition == SplineBoundaryCondition.ParabolicallyTerminated)
                && (rightBoundaryCondition == SplineBoundaryCondition.ParabolicallyTerminated)) {
            leftBoundaryCondition = SplineBoundaryCondition.SecondDerivative;
            leftBoundary = 0d;
            rightBoundaryCondition = SplineBoundaryCondition.SecondDerivative;
            rightBoundary = 0d;
        }
        if (leftBoundaryCondition == SplineBoundaryCondition.Natural) {
            leftBoundaryCondition = SplineBoundaryCondition.SecondDerivative;
            leftBoundary = 0d;
        }
        if (rightBoundaryCondition == SplineBoundaryCondition.Natural) {
            rightBoundaryCondition = SplineBoundaryCondition.SecondDerivative;
            rightBoundary = 0d;
        }
        final double[] a1 = new double[n];
        final double[] a2 = new double[n];
        final double[] a3 = new double[n];
        final double[] b = new double[n];
        // Left Boundary
        switch (leftBoundaryCondition) {
        case ParabolicallyTerminated:
            a1[0] = 0;
            a2[0] = 1;
            a3[0] = 1;
            b[0] = 2 * (y[1] - y[0]) / (x[1] - x[0]);
            break;
        case FirstDerivative:
            a1[0] = 0;
            a2[0] = 1;
            a3[0] = 0;
            b[0] = leftBoundary;
            break;
        case SecondDerivative:
            a1[0] = 0;
            a2[0] = 2;
            a3[0] = 1;
            b[0] = (3 * ((y[1] - y[0]) / (x[1] - x[0]))) - (0.5 * leftBoundary * (x[1] - x[0]));
            break;
        default:
            throw new UnsupportedOperationException("Invalid Left Boundary Condition "+leftBoundaryCondition);
        }
        // Central Conditions
        for (int i = 1; i < x.length - 1; i++) {
            a1[i] = x[i + 1] - x[i];
            a2[i] = 2 * (x[i + 1] - x[i - 1]);
            a3[i] = x[i] - x[i - 1];
            b[i] = (3 * (y[i] - y[i - 1]) / (x[i] - x[i - 1]) * (x[i + 1] - x[i]))
                    + (3 * (y[i + 1] - y[i]) / (x[i + 1] - x[i]) * (x[i] - x[i - 1]));
        }
        // Right Boundary
        switch (rightBoundaryCondition) {
        case ParabolicallyTerminated:
            a1[n - 1] = 1;
            a2[n - 1] = 1;
            a3[n - 1] = 0;
            b[n - 1] = 2 * (y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]);
            break;
        case FirstDerivative:
            a1[n - 1] = 0;
            a2[n - 1] = 1;
            a3[n - 1] = 0;
            b[n - 1] = rightBoundary;
            break;
        case SecondDerivative:
            a1[n - 1] = 1;
            a2[n - 1] = 2;
            a3[n - 1] = 0;
            b[n - 1] = (3 * (y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]))
                    + (0.5 * rightBoundary * (x[n - 1] - x[n - 2]));
            break;
        default:
            throw new UnsupportedOperationException("Invalid Right Boundary Condition "+rightBoundaryCondition);
        }

        // Build Spline
        double[] dd = solveTridiagonal(a1, a2, a3, b);
        return interpolateHermiteSorted(x, y, dd);
    }

    /**
     * Create a natural cubic spline interpolation from a set of (x,y) value pairs and zero second derivatives at the
     * two boundaries, sorted ascendingly by x.
     */
    public static CubicSpline interpolateNaturalSorted(double[] x, double[] y) {
        return interpolateBoundariesSorted(x, y, SplineBoundaryCondition.SecondDerivative, 0.0,
                SplineBoundaryCondition.SecondDerivative, 0.0);
    }

    /**
     * Three-Point Differentiation Helper.
     * 
     * @param xx
     *            Sample Points t.
     * @param yy
     *            Sample Values x(t).
     * @param indexT
     *            Index of the point of the differentiation.
     * @param index0
     *            Index of the first sample.
     * @param index1
     *            Index of the second sample.
     * @param index2
     *            Index of the third sample.
     * @return The derivative approximation.
     */
    private static double differentiateThreePoint(double[] xx, double[] yy, int indexT, int index0, int index1, int index2) {
        double x0 = yy[index0];
        double x1 = yy[index1];
        double x2 = yy[index2];
        double t = xx[indexT] - xx[index0];
        double t1 = xx[index1] - xx[index0];
        double t2 = xx[index2] - xx[index0];
        double a = (x2 - x0 - (t2 / t1 * (x1 - x0))) / (t2 * t2 - t1 * t2);
        double b = (x1 - x0 - a * t1 * t1) / t1;
        return (2 * a * t) + b;
    }

    /**
     * Tridiagonal Solve Helper.
     * 
     * @param a
     *            The a-vector[n].
     * @param b
     *            The b-vector[n], will be modified by this function.
     * @param c
     *            The c-vector[n].
     * @param d
     *            The d-vector[n], will be modified by this function.
     * @return The x-vector[n]
     */
    private static double[] solveTridiagonal(double[] a, double[] b, double[] c, double[] d) {
        for (int k = 1; k < a.length; k++) {
            double t = a[k] / b[k - 1];
            b[k] = b[k] - (t * c[k - 1]);
            d[k] = d[k] - (t * d[k - 1]);
        }
        final double[] x = new double[a.length];
        x[x.length - 1] = d[d.length - 1] / b[b.length - 1];
        for (int k = x.length - 2; k >= 0; k--) {
            x[k] = (d[k] - (c[k] * x[k + 1])) / b[k];
        }
        return x;
    }

    /**
     * Interpolate at point t.
     * 
     * @param t
     *            Point t to interpolate at.
     * @return Interpolated value x(t).
     */
    public double interpolate(double t) {
        int k = leftSegmentIndex(t);
        final double x = t - _x[k];
        return _c0[k] + x * (_c1[k] + x * (_c2[k] + x * _c3[k]));
    }

    /**
     * Differentiate at point t.
     * @param t Point t to interpolate at.
     * @return Interpolated first derivative at point t.
     */
    public double differentiate(double t) {
        int k = leftSegmentIndex(t);
        final double x = t - _x[k];
        return _c1[k] + x * (2 * _c2[k] + x * 3 * _c3[k]);
    }

    /**
     * Differentiate twice at point t.
     * @param t Point t to interpolate at.
     * @return Interpolated second derivative at point t.
     */
    public double differentiate2(double t) {
        int k = leftSegmentIndex(t);
        final double x = t - _x[k];
        return 2 * _c2[k] + x * 6 * _c3[k];
    }

    /**
     * Indefinite integral at point t.
     * 
     * @param t
     *            Point t to integrate at.
     */
    public double integrate(double t) {
        int k = leftSegmentIndex(t);
        final double x = t - _x[k];
        return _indefiniteIntegral.get()[k] + x * (_c0[k] + x * (_c1[k] / 2 + x * (_c2[k] / 3 + x * _c3[k] / 4)));
    }

    /**
     * Definite integral between points a and b.
     * 
     * @param a
     *            Left bound of the integration interval [a,b].
     * @param bRight
     *            bound of the integration interval [a,b].
     */
    public double integrate(double a, double b) {
        return integrate(b) - integrate(a);
    }

    private double[] computeIndefiniteIntegral() {
        final double[] integral = new double[_c1.length];
        for (int i = 0; i < integral.length - 1; i++) {
            double w = _x[i + 1] - _x[i];
            integral[i + 1] = integral[i] + w * (_c0[i] + w * (_c1[i] / 2 + w * (_c2[i] / 3 + w * _c3[i] / 4)));
        }
        return integral;
    }

    /**
     * Find the index of the greatest sample point smaller than t, or the left index of the closest segment for
     * extrapolation.
     */
    private int leftSegmentIndex(double t) {
        int index = Arrays.binarySearch(_x, t);
        if (index < 0) {
            index = ~index - 1;
        }
        return Math.min(Math.max(index, 0), _x.length - 2);
    }
}
