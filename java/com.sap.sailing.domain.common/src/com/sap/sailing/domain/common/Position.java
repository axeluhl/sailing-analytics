package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface Position extends Serializable {
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

    /**
     * Projects this position onto the great circle through <code>p</code> with bearing <code>bearing</code>. Note that
     * if the angle between this position and the great circle is 90 degrees then there is no solution, and a
     * <code>NaN</code> or exception will result.
     */
    Position projectToLineThrough(Position p, Bearing bearing);

    /**
     * @return an unsigned distance
     */
    Distance crossTrackError(Position p, Bearing bearing);

    /**
     * Computes how far along the great circle starting at <code>p</code> and pointing
     * to <code>bearing</code> one has to travel to reach the projection of this position
     * onto the great circle described by <code>p</code> and <code>bearing</code>. Note that
     * if the angle between this position and the great circle is 90 degrees then there is
     * no solution, and a <code>NaN</code> or exception will result.
     */
    Distance alongTrackDistance(Position p, Bearing bearing);
}
