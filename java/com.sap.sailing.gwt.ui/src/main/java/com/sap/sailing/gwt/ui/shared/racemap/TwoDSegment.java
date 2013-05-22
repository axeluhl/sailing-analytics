package com.sap.sailing.gwt.ui.shared.racemap;

/**
 * Represents a segment (or line) in the bidimensional space.
 * 
 * @author I077899 Bogdan Mihai
 * 
 */
public class TwoDSegment {
    private double lineSlope = 0.0;
    private double lineIntercept = 0.0;

    private TwoDPoint firstPoint = null;
    private TwoDPoint secondPoint = null;

    public TwoDSegment(TwoDPoint p1, TwoDPoint p2) {
        this(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public TwoDSegment(double x1, double y1, double x2, double y2) {

        if (x1 != x2) {
            this.lineSlope = (y2 - y1) / (x2 - x1);
            this.lineIntercept = (x2 * y1 - x1 * y2) / (x2 - x1);
        }

        // TODO: if x1 == x2 ...

        this.firstPoint = new TwoDPoint(x1, y1);
        this.secondPoint = new TwoDPoint(x2, y2);
    }

    public double getLineSlope() {
        return this.lineSlope;
    }

    public double getLineIntercept() {
        return this.lineIntercept;
    }

    public TwoDPoint getFirstPoint() {
        return this.firstPoint;
    }

    public TwoDPoint getSecondPoint() {
        return this.secondPoint;
    }

    public TwoDVector asVector() {
        return new TwoDVector(this.firstPoint, this.secondPoint);
    }

    public static boolean areIntersecting(TwoDSegment s1, TwoDSegment s2) {
        return TwoDPoint.areIntersecting(s1.firstPoint, s1.secondPoint, s2.firstPoint, s2.secondPoint);
    }

    public TwoDPoint getIntersection(TwoDSegment segment) {

        double xA = this.firstPoint.getX();
        double yA = this.firstPoint.getY();

        double xC = segment.firstPoint.getX();
        double yC = segment.firstPoint.getY();

        double m1 = this.lineSlope;
        double m2 = segment.lineSlope;

        double x = (yC - yA + m1 * xA - m2 * xC) / (m1 - m2);
        double y = (yC - yA + m2 * (xA - xC)) * m1 / (m1 - m2) + yA;

        return new TwoDPoint(x, y);
    }

    public boolean contains(TwoDPoint point, boolean firstProjectOnLine) {

        if (firstProjectOnLine) {
            point = point.getProjection(this);
        } else if (Math.abs(point.getY() - (this.lineSlope * point.getX() + this.lineIntercept)) > 0.00001) {
            return false;
        }

        double maxX = Math.max(this.firstPoint.getX(), this.secondPoint.getX());
        double minX = Math.min(this.firstPoint.getX(), this.secondPoint.getX());

        double maxY = Math.max(this.firstPoint.getY(), this.secondPoint.getY());
        double minY = Math.min(this.firstPoint.getY(), this.secondPoint.getY());

        return (minX <= point.getX() && point.getX() <= maxX) && (minY <= point.getY() && point.getY() <= maxY);
    }
}
