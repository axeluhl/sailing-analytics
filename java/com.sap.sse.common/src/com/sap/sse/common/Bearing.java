package com.sap.sse.common;

import java.io.Serializable;

import com.sap.sse.common.impl.DegreeBearingImpl;

public interface Bearing extends Serializable, Comparable<Bearing> {
    Bearing NORTH = new DegreeBearingImpl(0);
    Bearing EAST = new DegreeBearingImpl(90);
    Bearing SOUTH = new DegreeBearingImpl(180);
    Bearing WEST = new DegreeBearingImpl(270);

    double getDegrees();

    double getRadians();

    /**
     * adds or subtracts 180 degrees to obtain the opposite bearing
     */
    Bearing reverse();
    
    /**
     * Adds the bearing <code>diff</code> to this bearing and returns the new result. The result will be between 0 and 360 (inclusive) if
     * this bearing and <code>diff</code> are also within 0 and 360.
     */
    Bearing add(Bearing diff);
    
    /**
     * Computes the bearing difference that needs to be {@link #add(Bearing) added} to this bearing
     * to result in <code>b</code>. The absolute number of degrees of the resulting bearing is
     * guaranteed to be less or equal to 180. For example (simplifying bearings as the number
     * of degrees they represent), <code>180.getDifferenceTo(182)==2</code>, and
     * <code>180.getDifferenceTo(178)==-2</code>, and <code>10.getDifferenceTo(-10)==-20</code>,
     * and <code>350.getDifferenceTo(10)==20</code>, and <code>10.getDifferenceTo(350)==-20</code>
     */
    Bearing getDifferenceTo(Bearing b);
    
    /**
     * Like {@link #getDifferenceTo(Bearing)}, with the exception that the absolute number of degrees of the resulting
     * bearing will be > 180 in cases, when abs(lastDifference.getDegrees() - getDifferenceTo(b)) >= 180). Especially
     * for penalty circles, it is more likely to have a course change step sequence like 20, 70, 120, 200, 90, 20 which
     * produces 520 degrees total course change than a sequence with 20, 70, 120, -160, 90, 20 which produces 160
     * degrees total course change.
     */
    Bearing getDifferenceTo(Bearing b, Bearing lastDifference);

    /**
     * Finds the middle between this bearing and the <code>other</code> bearing. For degree differences
     * up to and including 180 degrees, the arithmetic mean between the angles is used to construct the
     * resulting bearing. Otherwise, the reverse of the arithmetic mean is returned.
     */
    Bearing middle(Bearing other);

    /**
     * If this bearing has a negative signum, reverts the signum. For example, {@code new DegreeBearingImpl(-10).abs().equals(new DegreeBearingImpl(10)}.
     */
    Bearing abs();

}
