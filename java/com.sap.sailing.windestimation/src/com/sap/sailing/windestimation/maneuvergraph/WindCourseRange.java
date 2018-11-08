package com.sap.sailing.windestimation.maneuvergraph;

public class WindCourseRange {

    private final double fromPortside;
    private final double angleTowardStarboard;

    public WindCourseRange(double fromPortside, double angleTowardStarboard) {
        this.fromPortside = fromPortside;
        this.angleTowardStarboard = angleTowardStarboard;
    }

    public double getFromPortside() {
        return fromPortside;
    }

    public double getAngleTowardStarboard() {
        return angleTowardStarboard;
    }

    public WindCourseRange invert() {
        double newFromPortside = fromPortside + angleTowardStarboard;
        if (newFromPortside >= 360) {
            newFromPortside -= 360;
        }
        double newAngleTowardStarboard = 360 - angleTowardStarboard;
        return new WindCourseRange(newFromPortside, newAngleTowardStarboard);
    }

    public double getAvgWindCourse() {
        double avgWindCourse = fromPortside + angleTowardStarboard / 2.0;
        if (avgWindCourse > 360) {
            avgWindCourse -= 360;
        }
        return avgWindCourse;
    }

    public IntersectedWindRange toIntersected() {
        return new IntersectedWindRange(fromPortside, angleTowardStarboard, 0);
    }

    // public IntersectedWindRange intersect(WindCourseRange nextWindRange) {
    // double deviationFromPortsideBoundaryTowardStarboard = nextWindRange.fromPortside - fromPortside;
    // if (deviationFromPortsideBoundaryTowardStarboard < 0) {
    // deviationFromPortsideBoundaryTowardStarboard += 360;
    // }
    // double deviationFromPortsideTowardStarboardInDegrees = deviationFromPortsideBoundaryTowardStarboard
    // - angleTowardStarboard;
    // double newFromPortside;
    // double newAngleTowardStarboard;
    // double violationRange;
    // if (deviationFromPortsideTowardStarboardInDegrees <= 0) {
    // // other.fromPortside is within the range
    // newFromPortside = nextWindRange.fromPortside;
    // violationRange = 0;
    // if (deviationFromPortsideTowardStarboardInDegrees + nextWindRange.angleTowardStarboard < 0) {
    // newAngleTowardStarboard = nextWindRange.angleTowardStarboard;
    // } else {
    // newAngleTowardStarboard = Math.abs(deviationFromPortsideTowardStarboardInDegrees);
    // }
    // } else {
    // double deviationFromPortsideBoundaryTowardPortside = 360 - deviationFromPortsideBoundaryTowardStarboard;
    // double deviationFromPortsideTowardPortsideInDegrees = deviationFromPortsideBoundaryTowardPortside
    // - nextWindRange.angleTowardStarboard;
    // if (deviationFromPortsideTowardPortsideInDegrees <= 0) {
    // // fromPortside is within the other range
    // newFromPortside = fromPortside;
    // violationRange = 0;
    // if (deviationFromPortsideTowardPortsideInDegrees + angleTowardStarboard < 0) {
    // newAngleTowardStarboard = angleTowardStarboard;
    // } else {
    // newAngleTowardStarboard = Math.abs(deviationFromPortsideTowardPortsideInDegrees);
    // }
    // } else {
    // newFromPortside = nextWindRange.fromPortside;
    // newAngleTowardStarboard = 5;
    // if (deviationFromPortsideTowardStarboardInDegrees < deviationFromPortsideTowardPortsideInDegrees) {
    // // newFromPortside = nextWindRange.angleTowardStarboard -
    // // deviationFromPortsideTowardStarboardInDegrees;
    // // newAngleTowardStarboard = deviationFromPortsideTowardStarboardInDegrees;
    // violationRange = deviationFromPortsideTowardStarboardInDegrees;
    // newFromPortside -= 5;
    // if(newFromPortside < 0) {
    // newFromPortside += 360;
    // }
    // } else {
    // // newFromPortside = angleTowardStarboard - deviationFromPortsideTowardPortsideInDegrees;
    // // newAngleTowardStarboard = deviationFromPortsideTowardPortsideInDegrees;
    // violationRange = deviationFromPortsideTowardPortsideInDegrees;
    // }
    // }
    // }
    // if(newAngleTowardStarboard < 10) {
    // double extension = 10 - newAngleTowardStarboard;
    // newAngleTowardStarboard = 10;
    // newFromPortside -= extension / 2.0;
    // }
    // return new IntersectedWindRange(newFromPortside, newAngleTowardStarboard, violationRange);
    // }

    public IntersectedWindRange intersect(WindCourseRange nextWindRange) {
        double deviationFromPortsideBoundaryTowardStarboard = nextWindRange.fromPortside - fromPortside;
        if (deviationFromPortsideBoundaryTowardStarboard < 0) {
            deviationFromPortsideBoundaryTowardStarboard += 360;
        }
        double deviationFromPortsideTowardStarboardInDegrees = deviationFromPortsideBoundaryTowardStarboard
                - angleTowardStarboard;
        double newFromPortside;
        double newAngleTowardStarboard;
        double violationRange;
        if (deviationFromPortsideTowardStarboardInDegrees <= 0) {
            violationRange = 0;
        } else {
            double deviationFromPortsideBoundaryTowardPortside = 360 - deviationFromPortsideBoundaryTowardStarboard;
            double deviationFromPortsideTowardPortsideInDegrees = deviationFromPortsideBoundaryTowardPortside
                    - nextWindRange.angleTowardStarboard;
            if (deviationFromPortsideTowardPortsideInDegrees <= 0) {
                violationRange = 0;
            } else {
                if (deviationFromPortsideTowardStarboardInDegrees < deviationFromPortsideTowardPortsideInDegrees) {
                    violationRange = deviationFromPortsideTowardStarboardInDegrees;
                } else {
                    violationRange = deviationFromPortsideTowardPortsideInDegrees;
                }
            }
        }
        newFromPortside = nextWindRange.fromPortside;
        newAngleTowardStarboard = nextWindRange.angleTowardStarboard;
        return new IntersectedWindRange(newFromPortside, newAngleTowardStarboard, violationRange);
    }

    public boolean isWindCourseWithinRange(double windCourseInDegrees) {
        double deviationFromPortsideBoundaryTowardStarboard = windCourseInDegrees - fromPortside;
        if (deviationFromPortsideBoundaryTowardStarboard < 0) {
            deviationFromPortsideBoundaryTowardStarboard += 360;
        }
        double deviationFromPortsideTowardStarboardInDegrees = deviationFromPortsideBoundaryTowardStarboard
                - angleTowardStarboard;
        if (deviationFromPortsideTowardStarboardInDegrees <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(angleTowardStarboard);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(fromPortside);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WindCourseRange other = (WindCourseRange) obj;
        if (Double.doubleToLongBits(angleTowardStarboard) != Double.doubleToLongBits(other.angleTowardStarboard))
            return false;
        if (Double.doubleToLongBits(fromPortside) != Double.doubleToLongBits(other.fromPortside))
            return false;
        return true;
    }

}
