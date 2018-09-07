package com.sap.sailing.windestimation.maneuvergraph.pointofsail;

public class WindCourseRange {

    private final double windCourseOnPortsideBoundary;
    private final double windCourseDeviationTowardStarboardInDegrees;
    private final double secondsPassedSincePortsideBoundaryRecord;
    private final double secondsPassedSinceStarboardBoundaryRecord;

    public WindCourseRange(double windCourseOnPortsideBoundary, double windCourseDeviationTowardStarboardInDegrees,
            double secondsPassedSincePortsideBoundaryRecord, double secondsPassedSinceStarboardBoundaryRecord) {
        this.windCourseOnPortsideBoundary = windCourseOnPortsideBoundary;
        this.windCourseDeviationTowardStarboardInDegrees = windCourseDeviationTowardStarboardInDegrees;
        this.secondsPassedSincePortsideBoundaryRecord = secondsPassedSincePortsideBoundaryRecord;
        this.secondsPassedSinceStarboardBoundaryRecord = secondsPassedSinceStarboardBoundaryRecord;
    }

    public WindCourseRange calculateForNextGraphLevel(double nextWindCourse, double secondsPassedSincePreviousManeuver) {
        double deviationFromPortsideBoundaryTowardStarboard = nextWindCourse - windCourseOnPortsideBoundary;
        if (deviationFromPortsideBoundaryTowardStarboard < 0) {
            deviationFromPortsideBoundaryTowardStarboard += 360;
        }
        double deviationFromRecordedWindCourseDeviationTowardStarboardInDegrees = deviationFromPortsideBoundaryTowardStarboard
                - windCourseDeviationTowardStarboardInDegrees;
        if (deviationFromRecordedWindCourseDeviationTowardStarboardInDegrees <= 0) {
            // new wind course is within the previous wind deviation range
            return new WindCourseRange(windCourseOnPortsideBoundary, windCourseDeviationTowardStarboardInDegrees,
                    secondsPassedSincePortsideBoundaryRecord + secondsPassedSincePreviousManeuver,
                    secondsPassedSinceStarboardBoundaryRecord + secondsPassedSincePreviousManeuver);
        } else {
            double deviationFromRecordedPortsideBoundaryTowardPortside = 360
                    - deviationFromPortsideBoundaryTowardStarboard;
            if (deviationFromRecordedWindCourseDeviationTowardStarboardInDegrees > deviationFromRecordedPortsideBoundaryTowardPortside) {
                return new WindCourseRange(nextWindCourse,
                        windCourseDeviationTowardStarboardInDegrees
                                + deviationFromRecordedPortsideBoundaryTowardPortside,
                        0, secondsPassedSinceStarboardBoundaryRecord + secondsPassedSincePreviousManeuver);
            } else {
                return new WindCourseRange(windCourseOnPortsideBoundary, deviationFromPortsideBoundaryTowardStarboard,
                        secondsPassedSincePortsideBoundaryRecord + secondsPassedSincePreviousManeuver, 0);
            }
        }
    }

    public boolean isCalculatedWithinLastSeconds(double lastSeconds) {
        return secondsPassedSinceStarboardBoundaryRecord < lastSeconds
                && secondsPassedSincePortsideBoundaryRecord < lastSeconds;
    }

    public double getWindCourseDeviationRangeInDegrees() {
        return windCourseDeviationTowardStarboardInDegrees;
    }

}
