import java.util.Arrays;
import java.util.Collections;
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

/// <summary>
/// Cubic Spline Interpolation.
/// </summary>
/// <remarks>Supports both differentiation and integration.</remarks>
public class CubicSpline {
    /**
     * Left and right boundary conditions.
     */
    public enum SplineBoundaryCondition {
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
    
    final double[] _x;
    final double[] _c0;
    final double[] _c1;
    final double[] _c2;
    final double[] _c3;
    Supplier<double[]>_indefiniteIntegral;

    /// <param name="x">sample points (N+1), sorted ascending</param>
    /// <param name="c0">Zero order spline coefficients (N)</param>
    /// <param name="c1">First order spline coefficients (N)</param>
    /// <param name="c2">second order spline coefficients (N)</param>
    /// <param name="c3">third order spline coefficients (N)</param>
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
        _indefiniteIntegral = this::ComputeIndefiniteIntegral;
    }

    /// <summary>
    /// Create a Hermite cubic spline interpolation from a set of (x,y) value pairs and their slope (first derivative),
    /// sorted ascendingly by x.
    /// </summary>
    public static CubicSpline InterpolateHermiteSorted(double[] x, double[] y, double[] firstDerivatives) {
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

    /// <summary>
    /// Create a Hermite cubic spline interpolation from an unsorted set of (x,y) value pairs and their slope (first
    /// derivative).
    /// WARNING: Works in-place and can thus causes the data array to be reordered.
    /// </summary>
    public static CubicSpline InterpolateHermiteInplace(double[] x, double[] y, double[] firstDerivatives) {
        if (x.length != y.length || x.length != firstDerivatives.length) {
            throw new IllegalArgumentException("Argument vectors must have same length");
        }
        if (x.length < 2) {
            throw new IllegalArgumentException("Array too small, must be at least two but was "+x.length);
        }
        Collections.sort(x, y, firstDerivatives);
        return InterpolateHermiteSorted(x, y, firstDerivatives);
    }

    /// <summary>
    /// Create a Hermite cubic spline interpolation from an unsorted set of (x,y) value pairs and their slope (first
    /// derivative).
    /// </summary>
    public static CubicSpline InterpolateHermite(IEnumerable<double> x, IEnumerable<double> y, IEnumerable<double> firstDerivatives)
        {
            // note: we must make a copy, even if the input was arrays already
            return InterpolateHermiteInplace(x.ToArray(), y.ToArray(), firstDerivatives.ToArray());
        }

    /// <summary>
    /// Create an Akima cubic spline interpolation from a set of (x,y) value pairs, sorted ascendingly by x.
    /// Akima splines are robust to outliers.
    /// </summary>
    public static CubicSpline InterpolateAkimaSorted(double[] x, double[] y) {
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
            dd[i] = weights[i - 1].AlmostEqual(0.0) && weights[i + 1].AlmostEqual(0.0)
                    ? (((x[i + 1] - x[i]) * diff[i - 1]) + ((x[i] - x[i - 1]) * diff[i])) / (x[i + 1] - x[i - 1])
                    : ((weights[i + 1] * diff[i - 1]) + (weights[i - 1] * diff[i])) / (weights[i + 1] + weights[i - 1]);
        }
        dd[0] = DifferentiateThreePoint(x, y, 0, 0, 1, 2);
        dd[1] = DifferentiateThreePoint(x, y, 1, 0, 1, 2);
        dd[x.length - 2] = DifferentiateThreePoint(x, y, x.length - 2, x.length - 3, x.length - 2, x.length - 1);
        dd[x.length - 1] = DifferentiateThreePoint(x, y, x.length - 1, x.length - 3, x.length - 2, x.length - 1);
        /* Build Akima spline using Hermite interpolation scheme */
        return InterpolateHermiteSorted(x, y, dd);
    }

    /// <summary>
    /// Create an Akima cubic spline interpolation from an unsorted set of (x,y) value pairs.
    /// Akima splines are robust to outliers.
    /// WARNING: Works in-place and can thus causes the data array to be reordered.
    /// </summary>
    public static CubicSpline InterpolateAkimaInplace(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Argument vectors must have same length");
        }
        Sorting.Sort(x, y);
        return InterpolateAkimaSorted(x, y);
    }

    /// <summary>
    /// Create a cubic spline interpolation from a set of (x,y) value pairs, sorted ascendingly by x,
    /// and custom boundary/termination conditions.
    /// </summary>
    public static CubicSpline InterpolateBoundariesSorted(double[] x, double[] y,
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
        double[] dd = SolveTridiagonal(a1, a2, a3, b);
        return InterpolateHermiteSorted(x, y, dd);
    }

    /// <summary>
    /// Create a natural cubic spline interpolation from a set of (x,y) value pairs
    /// and zero second derivatives at the two boundaries, sorted ascendingly by x.
    /// </summary>
    public static CubicSpline InterpolateNaturalSorted(double[] x, double[] y) {
        return InterpolateBoundariesSorted(x, y, SplineBoundaryCondition.SecondDerivative, 0.0,
                SplineBoundaryCondition.SecondDerivative, 0.0);
    }

    /// <summary>
    /// Three-Point Differentiation Helper.
    /// </summary>
    /// <param name="xx">Sample Points t.</param>
    /// <param name="yy">Sample Values x(t).</param>
    /// <param name="indexT">Index of the point of the differentiation.</param>
    /// <param name="index0">Index of the first sample.</param>
    /// <param name="index1">Index of the second sample.</param>
    /// <param name="index2">Index of the third sample.</param>
    /// <returns>The derivative approximation.</returns>
    static double DifferentiateThreePoint(double[] xx, double[] yy, int indexT, int index0, int index1, int index2) {
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

    /// <summary>
    /// Tridiagonal Solve Helper.
    /// </summary>
    /// <param name="a">The a-vector[n].</param>
    /// <param name="b">The b-vector[n], will be modified by this function.</param>
    /// <param name="c">The c-vector[n].</param>
    /// <param name="d">The d-vector[n], will be modified by this function.</param>
    /// <returns>The x-vector[n]</returns>
    static double[] SolveTridiagonal(double[] a, double[] b, double[] c, double[] d) {
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

    /// <summary>
    /// Interpolate at point t.
    /// </summary>
    /// <param name="t">Point t to interpolate at.</param>
    /// <returns>Interpolated value x(t).</returns>
    public double Interpolate(double t) {
        int k = LeftSegmentIndex(t);
        final double x = t - _x[k];
        return _c0[k] + x * (_c1[k] + x * (_c2[k] + x * _c3[k]));
    }

    /// <summary>
    /// Differentiate at point t.
    /// </summary>
    /// <param name="t">Point t to interpolate at.</param>
    /// <returns>Interpolated first derivative at point t.</returns>
    public double Differentiate(double t) {
        int k = LeftSegmentIndex(t);
        final double x = t - _x[k];
        return _c1[k] + x * (2 * _c2[k] + x * 3 * _c3[k]);
    }

    /// <summary>
    /// Differentiate twice at point t.
    /// </summary>
    /// <param name="t">Point t to interpolate at.</param>
    /// <returns>Interpolated second derivative at point t.</returns>
    public double Differentiate2(double t) {
        int k = LeftSegmentIndex(t);
        final double x = t - _x[k];
        return 2 * _c2[k] + x * 6 * _c3[k];
    }

    /// <summary>
    /// Indefinite integral at point t.
    /// </summary>
    /// <param name="t">Point t to integrate at.</param>
    public double Integrate(double t) {
        int k = LeftSegmentIndex(t);
        final double x = t - _x[k];
        return _indefiniteIntegral.get()[k] + x * (_c0[k] + x * (_c1[k] / 2 + x * (_c2[k] / 3 + x * _c3[k] / 4)));
    }

    /// <summary>
    /// Definite integral between points a and b.
    /// </summary>
    /// <param name="a">Left bound of the integration interval [a,b].</param>
    /// <param name="b">Right bound of the integration interval [a,b].</param>
    public double Integrate(double a, double b) {
        return Integrate(b) - Integrate(a);
    }

    private double[] ComputeIndefiniteIntegral() {
        final double[] integral = new double[_c1.length];
        for (int i = 0; i < integral.length - 1; i++) {
            double w = _x[i + 1] - _x[i];
            integral[i + 1] = integral[i] + w * (_c0[i] + w * (_c1[i] / 2 + w * (_c2[i] / 3 + w * _c3[i] / 4)));
        }
        return integral;
    }

    /// <summary>
    /// Find the index of the greatest sample point smaller than t,
    /// or the left index of the closest segment for extrapolation.
    /// </summary>
    private int LeftSegmentIndex(double t) {
        int index = Arrays.binarySearch(_x, t);
        if (index < 0) {
            index = ~index - 1;
        }
        return Math.min(Math.max(index, 0), _x.length - 2);
    }
}
