package com.sap.sailing.windestimation.maneuvergraph;

public class IntersectedWindRange extends WindCourseRange {

    private static final double MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES = 40;
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

    public double getPenaltyFactorForTransition(double secondsPassed) {
        double violationRange = getViolationRange();
        double penaltyFactor;
        if (violationRange == 0) {
            penaltyFactor = 1.0;
        } else {
            // violationRange -= Math.max(1, secondsPassed / 3600)
            // * MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES;
            // if (violationRange <= MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES) {
            // penaltyFactor = 1 / (1
            // + (MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES + violationRange)
            // * 0.1);
            if (violationRange <= MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES) {
                penaltyFactor = 1 / (1 + Math.pow(violationRange / MAX_ABS_WIND_COURSE_DEVIATION_TOLERANCE_WITHIN_ANALYSIS_INTERVAL_IN_DEGREES * 2, 2));
            } else {
                penaltyFactor = 1 / (1 + (Math.pow(violationRange, 2)));
            }
        }
        assert (penaltyFactor > 0.0001);
        return penaltyFactor;
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
