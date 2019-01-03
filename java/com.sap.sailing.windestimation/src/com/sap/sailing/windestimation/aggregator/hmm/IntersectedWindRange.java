package com.sap.sailing.windestimation.aggregator.hmm;

public class IntersectedWindRange extends WindCourseRange {

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(violationRange);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntersectedWindRange other = (IntersectedWindRange) obj;
        if (Double.doubleToLongBits(violationRange) != Double.doubleToLongBits(other.violationRange))
            return false;
        return true;
    }

}
