package com.sap.sailing.gwt.ui.simulator.racemap;

/**
 * Represents a point in the bidimensional space
 * 
 * @author I077899 Bogdan Mihai
 * 
 */
public class TwoDPoint {
    private double x;
    private double y;

    public TwoDPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getDistanceTo(TwoDPoint point) {
        return distanceBetween(this.x, this.y, point.getX(), point.getY());
    }

    public double getDistanceTo(double x, double y) {
        return distanceBetween(this.x, this.y, x, y);
    }

    public static double distanceBetween(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    public static double distanceBetween(TwoDPoint point1, TwoDPoint point2) {
        return distanceBetween(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    public double getDistanceTo(TwoDSegment line) {

        return this.getDistanceTo(this.getProjection(line));

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        TwoDPoint other = (TwoDPoint) obj;
        return this.x == other.getX() && this.y == other.getY();
    }

    @Override
    public int hashCode() {
        return 31 * (Double.valueOf(this.x).hashCode()) * (Double.valueOf(this.y).hashCode());
    }

    public TwoDPoint getProjection(TwoDSegment line) {

        double slope = line.getLineSlope();
        double intercept = line.getLineIntercept();

        double x = (slope * this.getY() + this.getX() - slope * intercept) / (slope * slope + 1);
        double y = slope * x + intercept;

        return new TwoDPoint(x, y);
    }

    public TwoDPoint getProjectionByVector(TwoDSegment line, TwoDVector vector) {

        double a = vector.getRe();
        double b = vector.getIm();
        double p = line.getLineSlope();
        double q = line.getLineIntercept();

        double x = ((q - this.y) / b + this.x / a) / (1 / a - p / b);
        double y = p * x + q;

        return new TwoDPoint(x, y);
    }

    private static boolean areInClockwiseOrder(TwoDPoint A, TwoDPoint B, TwoDPoint C) {
        return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x);
    }

    public static boolean areIntersecting(TwoDPoint A, TwoDPoint B, TwoDPoint C, TwoDPoint D) {
        return (areInClockwiseOrder(A, C, D) != areInClockwiseOrder(B, C, D)) && (areInClockwiseOrder(A, B, C) != areInClockwiseOrder(A, B, D));
    }
}
