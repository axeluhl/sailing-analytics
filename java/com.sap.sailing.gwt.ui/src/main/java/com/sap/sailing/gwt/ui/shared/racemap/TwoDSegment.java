package com.sap.sailing.gwt.ui.shared.racemap;


public class TwoDSegment {
    private double lineSlope = 0.0;
    private double lineIntercept = 0.0;

    private TwoDPoint firstPoint = null;
    private TwoDPoint secondPoint = null;

    public TwoDSegment(final TwoDPoint p1, final TwoDPoint p2) {
        this(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public TwoDSegment(final double x1, final double y1, final double x2, final double y2) {
        if (x1 == x2) {
            // FIXME:
            // this.slope = Double.MAX_VALUE;

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

    public TwoDPoint projectionOfPointOnLine(final TwoDPoint p) {
        final double x = (this.lineSlope * p.getY() + p.getX() - this.lineSlope * this.lineIntercept)
                / (this.lineSlope * this.lineSlope + 1);
        final double y = this.lineSlope * x + this.lineIntercept;

        return new TwoDPoint(x, y);
    }

    public double distanceToLine(final TwoDPoint p) {
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

    public static boolean areIntersecting(final TwoDSegment s1, final TwoDSegment s2) {
        return TwoDPoint.areIntersecting(s1.firstPoint, s1.secondPoint, s2.firstPoint, s2.secondPoint);
    }

    public TwoDPoint intersectionPointWith(final TwoDSegment segment2) {
        return getIntersectionPoint(this, segment2);
    }

    public static TwoDPoint getIntersectionPoint(final TwoDSegment segment1, final TwoDSegment segment2) {

        final double x1 = segment1.firstPoint.getX();
        final double x2 = segment1.secondPoint.getX();
        final double x3 = segment2.firstPoint.getX();
        final double x4 = segment2.secondPoint.getX();

        final double y1 = segment1.firstPoint.getY();
        final double y2 = segment1.secondPoint.getY();
        final double y3 = segment2.firstPoint.getY();
        final double y4 = segment2.secondPoint.getY();

        final double temp1 = (y2 - y1) / (x2 - x1);
        final double temp2 = (y4 - y3) / (x4 - x3);

        final double x = (y3 - y1 + x1 * temp1 - x3 * temp2) / (temp1 - temp2);
        final double y = x * temp1 + y1 - x1 * temp1;

        return new TwoDPoint(x, y);
    }

    public boolean contains(TwoDPoint point) {
        double maxX = Math.max(this.firstPoint.getX(), this.secondPoint.getX());
        double minX = Math.min(this.firstPoint.getX(), this.secondPoint.getX());

        double maxY = Math.max(this.firstPoint.getY(), this.secondPoint.getY());
        double minY = Math.min(this.firstPoint.getY(), this.secondPoint.getY());

        return (minX <= point.getX() && point.getX() <= maxX) && (minY <= point.getY() && point.getY() <= maxY);
    }
}
