package com.sap.sailing.windestimation.aggregator.hmm;

/**
 * Represents result of a merge of two {@link WindCourseRange}-instances by means of
 * {@link WindCourseRange#intersect(WindCourseRange, CombinationModeOnViolation)}. The {@link #getViolationRange()}
 * contains the TWD delta which can be used for transition probability derivation.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class IntersectedWindRange extends WindCourseRange {

    private final double violationRange;

    public IntersectedWindRange(double fromPortside, double angleTowardStarboard, double violationRange) {
        super(fromPortside, angleTowardStarboard);
        this.violationRange = violationRange;
    }

    /**
     * When two wind ranges are {@link #intersect(WindCourseRange, CombinationModeOnViolation) intersected} and they
     * have no overlap ("empty intersection"), this method returns the smallest angle between them. This is also the
     * case if the intersection was computed with mode {@link CombinationModeOnViolation#EXPANSION}. If the ranges that
     * were intersected did have an overlap, this method returns {@code 0}. The difference between the two modes in case
     * of a violation is that with {@link CombinationModeOnViolation#EXPANSION} the resulting range is the union of both
     * ranges plus the gap; with {@link CombinationModeOnViolation#INTERSECTION} the resulting range describes only the
     * gap.
     * 
     * @return the size of the gap between the wind ranges intersected, in degrees
     */
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

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(super.toString());
        if (isViolation()) {
            result.append(" (");
            result.append(getViolationRange());
            result.append("° violation)");
        }
        return result.toString();
    }
}
