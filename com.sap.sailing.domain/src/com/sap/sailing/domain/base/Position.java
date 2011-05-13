package com.sap.sailing.domain.base;

public interface Position {
    double getLatRad();

    double getLngRad();

    double getLatDeg();

    double getLngDeg();

    /**
     * Central angle between this position and the other position
     */
    double getCentralAngleRad(Position p);

    Distance getDistance(Position p);
    
    /**
     * The bearing from this position towards <code>p</code> on a great circle
     */
    Bearing getBearingGreatCircle(Position p);

    /**
     * Translates along a rhumb line, assuming travel at constant bearing. This
     * is therfore not translating along a great circle but as a straight line, e.g.,
     * on a Marcator projection.
     */
    Position translateRhumb(Bearing bearing, Distance distance);

    /**
     * Translates along a great circle, assuming travel at varying bearing where
     * <code>bearing</code> tells the bearing at this position. Invariant:
     * <code>translate(getBearingGreatCircle(p), getDistance(p)).equals(p)</code>
     */
    Position translateGreatCircle(Bearing bearing, Distance distance);
}
