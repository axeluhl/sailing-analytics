package com.sap.sse.common.util;

import java.util.Arrays;


/*
    <copyright file="CubicSpline.cs" company="Math.NET">
    Math.NET Numerics, part of the Math.NET Project
    http://numerics.mathdotnet.com
    http://github.com/mathnet/mathnet-numerics
    
    Copyright (c) 2009-2014 Math.NET
    
    Permission is hereby granted, free of charge, to any person
    obtaining a copy of this software and associated documentation
    files (the "Software"), to deal in the Software without
    restriction, including without limitation the rights to use,
    copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the
    Software is furnished to do so, subject to the following
    conditions:
    
    The above copyright notice and this permission notice shall be
    included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
    EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
    HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
    FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
    OTHER DEALINGS IN THE SOFTWARE.
    </copyright>
*/

/**
 * This class creates a cubic spline interpolator for a set of inputs, values and possibly derivatives. The API allows
 * different methods to interpolate a value for an input.
 * 
 * The implementation is based on the CubicSpline.cs code in the Math.NET Numerics libraries. See <a href=
 * "github.com">https://github.com/mathnet/mathnet-numerics/blob/master/src/Numerics/Interpolation/CubicSpline.cs"</a>
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public class CubicSpline {

    private final double[] x;
    private final double[] c0;
    private final double[] c1;
    private final double[] c2;
    private final double[] c3;
    @SuppressWarnings("unused")
    private final double[] indefiniteIntegral;

    /**
     * 
     * @param x
     *            sample points, sorted ascending
     * @param c0
     *            zero order spline coefficients
     * @param c1
     *            first order spline coefficients
     * @param c2
     *            second order spline coefficients
     * @param c3
     *            third order spline coefficients
     */
    public CubicSpline(double[] x, double[] c0, double[] c1, double[] c2, double[] c3) {
        super();
        this.x = x;
        this.c0 = c0;
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        indefiniteIntegral = computeIndefiniteIntegral();
    }

    private double[] computeIndefiniteIntegral() {
        double[] integral = new double[c1.length];
        for (int i = 0; i < integral.length - 1; i++) {
            double w = x[i + 1] - x[i];
            integral[i + 1] = integral[i] + w * (c0[i] + w * (c1[i] / 2 + w * (c2[i] / 3 + w * c3[i] / 4)));
        }

        return integral;
    }

    /**
     * Find the index of the greatest sample point smaller than t, or the left index of the closest segment for
     * extrapolation.
     */
    private int leftSegmentIndex(double t) {
        int index = Arrays.binarySearch(x, t);
        if (index < 0) {
            index = ~index - 1;
        }

        return Math.min(Math.max(index, 0), x.length - 2);
    }

    /// <summary>
    /// Interpolate at point t.
    /// </summary>
    /// <param name="t">Point t to interpolate at.</param>
    /// <returns>Interpolated value x(t).</returns>
    /**
     * Interpolate at point t.
     * 
     * @param t
     *            Point t to interpolate at.
     * @return Interpolated value x(t).
     */
    public double interpolate(double t) {
        int k = leftSegmentIndex(t);
        double a = t - x[k];
        return c0[k] + a * (c1[k] + a * (c2[k] + a * c3[k]));
    }

}
