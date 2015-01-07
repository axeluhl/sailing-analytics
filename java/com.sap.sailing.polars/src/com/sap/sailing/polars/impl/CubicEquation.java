package com.sap.sailing.polars.impl;

public class CubicEquation {

    private double a;
    private double b;
    private double c;
    private double d;

    public CubicEquation(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    /**
     * Following: http://www.1728.org/cubic2.htm
     * !The complex roots are not returned!
     */
    public double[] solve() {

        double f = ((3 * c) / a - (b * b) / (a * a)) / 3;
        double g = ((2 * b * b * b) / (a * a * a) - (9 * b * c) / (a * a) + (27 * d) / a) / 27;
        double h = (g*g) / 4 + (f*f*f) / 27;
        
        double[] result;
        
        if (f == 0 && g == 0 && h == 0) {
            // All roots equal and real
            result = new double[1];
            result[0] = - Math.cbrt(d/a);
        } else if (h > 0) {
            // Only one root is real
            double r = -(g/2) + Math.sqrt(h);
            double s = Math.cbrt(r);
            double t = -(g/2) - Math.sqrt(h);
            double u = Math.cbrt(t);
            result = new double[1];
            result[0] = (s + u) - (b / (3 * a));
            //!!The complex roots are not returned
        } else {
            // All three roots are real and not equal
            double i = Math.sqrt((g * g) / 4 - h);
            double j = Math.cbrt(i);
            double k = Math.acos(-(g/(2*i)));
            double l = -1 * j;
            double m = Math.cos(k / 3);
            double n = Math.sqrt(3) * Math.sin(k/3);
            double p = - (b/(3*a));
            result = new double[3];
            result[0] = 2 * j * Math.cos(k / 3) - b / (3 * a);
            result[1] = l * (m + n) + p;
            result[2] = l * (m - n) + p;
        }
        return result;
    }

}
