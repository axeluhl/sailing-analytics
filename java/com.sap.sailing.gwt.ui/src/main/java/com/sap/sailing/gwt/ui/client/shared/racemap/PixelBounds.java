package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.maptypes.Projection;

/**
 * A bounding box for a map, but instead of using latitude/longitude coordinates like in {@link LatLngBounds}, these
 * bounds use the corresponding container pixel coordinates as obtained from
 * {@link Projection#fromLatLngToPoint(com.google.gwt.maps.client.base.LatLng, com.google.gwt.maps.client.base.Point)}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PixelBounds {
    private final Point upperLeft;
    private final Point lowerRight;
    
    public PixelBounds(Point upperLeft, Point lowerRight) {
        super();
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
    }
    
    /**
     * Creates a zero-size pixel bounds object by first mapping {@code singlePoint} through the {@link Projection}.
     */
    public PixelBounds(Projection projection, LatLng singlePoint) {
        this(projection.fromLatLngToPoint(singlePoint, Point.newInstance(0, 0)));
    }

    public PixelBounds(Point singlePoint) {
        this(singlePoint, singlePoint);
    }

    public Point getUpperLeft() {
        return upperLeft;
    }
    
    public Point getLowerLeft() {
        return Point.newInstance(getUpperLeft().getX(), getLowerRight().getY());
    }

    public Point getLowerRight() {
        return lowerRight;
    }
    
    public Point getUpperRight() {
        return Point.newInstance(getLowerRight().getX(), getUpperLeft().getY());
    }

    public PixelBounds extend(PixelBounds other) {
        final Point extendedUpperLeft = Point.newInstance(Math.min(this.getUpperLeft().getX(), other.getUpperLeft().getX()),
                Math.min(this.getUpperLeft().getY(), other.getUpperLeft().getY()));
        final Point extendedLowerRight = Point.newInstance(Math.max(this.getLowerRight().getX(), other.getLowerRight().getX()),
                Math.max(this.getLowerRight().getY(), other.getLowerRight().getY()));
        return new PixelBounds(extendedUpperLeft, extendedLowerRight);
    }
    
    public Point getCenter() {
        return Point.newInstance((getUpperLeft().getX()+getLowerRight().getX())/2.0,
                                 (getUpperLeft().getY()+getLowerRight().getY())/2.0);
    }
    
    public LatLng getLatLngCenter(Projection projection) {
        return projection.fromPointToLatLng(getCenter(), /* nowrap */ false);
    }

    public boolean contains(PixelBounds other) {
        return other.getUpperLeft().getX() >= this.getUpperLeft().getX()
            && other.getUpperLeft().getY() >= this.getUpperLeft().getY()
            && other.getLowerRight().getX() <= this.getLowerRight().getX()
            && other.getLowerRight().getX() <= this.getLowerRight().getX();
    }

    public LatLngBounds getLatLngBounds(Projection projection) {
        return LatLngBounds.newInstance(projection.fromPointToLatLng(getLowerLeft(), /* nowrap */ false),
                                        projection.fromPointToLatLng(getUpperRight(), /* nowrap */ false));
    }
}
