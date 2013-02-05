package com.sap.sailing.gwt.ui.shared.racemap;


public class TwoDSegment {
    private double lineSlope = 0.0;
    private double lineIntercept = 0.0;

    private TwoDPoint firstPoint = null;
    private TwoDPoint secondPoint = null;

    public TwoDSegment(TwoDPoint p1, TwoDPoint p2) {
        this(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public TwoDSegment(double x1, double y1, double x2, double y2) {
        if (x1 == x2) {
            // FIXME:
        } else {
            this.lineSlope = (y2 - y1) / (x2 - x1);
            this.lineIntercept = (x2 * y1 - x1 * y2) / (x2 - x1);
        }

        this.firstPoint = new TwoDPoint(x1, y1);
        this.secondPoint = new TwoDPoint(x2, y2);
    }

    public double getLineSlope() {
        return this.lineSlope;
    }

    public double getLineIntercept() {
        return this.lineIntercept;
    }

    public TwoDPoint projectionOfPointOnLine(TwoDPoint p) {
        double x = (this.lineSlope * p.getY() + p.getX() - this.lineSlope * this.lineIntercept)
                / (this.lineSlope * this.lineSlope + 1);
        double y = this.lineSlope * x + this.lineIntercept;

        return new TwoDPoint(x, y);
    }

    public double distanceToLine(TwoDPoint p) {
        return TwoDPoint.distanceBetween(p, this.projectionOfPointOnLine(p));
    }

    public TwoDPoint getFirstPoint() {
        return this.firstPoint;
    }

    public TwoDPoint getSecondPoint() {
        return this.secondPoint;
    }

    @Override
    public String toString() {
        return "Segment[" + this.firstPoint.toString() + "|" + this.secondPoint.toString() + "]";
    }

    public TwoDVector asVector() {
        return new TwoDVector(this.firstPoint, this.secondPoint);
    }

    public static boolean areIntersecting(TwoDSegment s1, TwoDSegment s2) {
        return TwoDPoint.areIntersecting(s1.firstPoint, s1.secondPoint, s2.firstPoint, s2.secondPoint);
    }

    public TwoDPoint intersectionPointWith(TwoDSegment segment2) {
        return getIntersection(this, segment2);
    }

    public static TwoDPoint getIntersection(TwoDSegment segment1, TwoDSegment segment2) {

        double xA = segment1.firstPoint.getX();
        double yA = segment1.firstPoint.getY();

        double xC = segment2.firstPoint.getX();
        double yC = segment2.firstPoint.getY();

        double m1 = segment1.lineSlope;
        double m2 = segment2.lineSlope;

        double x = (yC - yA + m1 * xA - m2 * xC) / (m1 - m2);
        double y = (yC - yA + m2 * (xA - xC)) * m1 / (m1 - m2) + yA;

        return new TwoDPoint(x, y);
    }

    public boolean contains(TwoDPoint point) {

        if (Math.abs(point.getY() - (this.lineSlope * point.getX() + this.lineIntercept)) > 0.00001) {
            return false;
        }

        double maxX = Math.max(this.firstPoint.getX(), this.secondPoint.getX());
        double minX = Math.min(this.firstPoint.getX(), this.secondPoint.getX());

        double maxY = Math.max(this.firstPoint.getY(), this.secondPoint.getY());
        double minY = Math.min(this.firstPoint.getY(), this.secondPoint.getY());

        return (minX <= point.getX() && point.getX() <= maxX) && (minY <= point.getY() && point.getY() <= maxY);
    }
}
