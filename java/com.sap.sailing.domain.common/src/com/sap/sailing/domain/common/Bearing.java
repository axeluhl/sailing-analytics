package com.sap.sailing.domain.common;

import java.io.Serializable;

import com.sap.sailing.domain.common.impl.DegreeBearingImpl;

public interface Bearing extends Serializable {
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
     * Finds the middle between this bearing and the <code>other</code> bearing. For degree differences
     * up to and including 180 degrees, the arithmetic mean between the angles is used to construct the
     * resulting bearing. Otherwise, the reverse of the arithmetic mean is returned.
     */
    Bearing middle(Bearing other);

}
