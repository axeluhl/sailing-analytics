package com.sap.sailing.windestimation.maneuvergraph.maneuvernode;

public class IntersectedWindRange extends WindRangeForManeuverNode {

    private final double violationRange;

    public IntersectedWindRange(double fromPortside, double angleTowardStarboard, double violationRange) {
        super(fromPortside, angleTowardStarboard);
        this.violationRange = violationRange;
    }

    public double getViolationRange() {
        return violationRange;
    }
    
    public boolean isViolation() {
        return violationRange > 0;
    }

}
