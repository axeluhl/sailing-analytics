package com.sap.sailing.domain.common;

public interface Bounds {
    Position getNorthEast();
    Position getSouthWest();
    Position getNorthWest();
    Position getSouthEast();
    Bounds intersect(Bounds other);
    Bounds union(Bounds other);
    boolean intersects(Bounds other);
    boolean contains(Position other);
    boolean contains(Bounds other);
    boolean isCrossesDateLine();
    boolean containsLatDeg(double latDeg);
    boolean containsLngDeg(double lngDeg);
}
