package com.sap.sailing.util;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

/**
 * Util class which is used for bearing calculation considering positions within a GPS track of a competitor.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class TrackedRaceUtil {
    /**
     * Gets a list of bearings between the provided time range (inclusive the boundaries). The bearings are retrieved by
     * means of {@link GPSFixTrack#getEstimatedSpeed(TimePoint)} with the provided frequency between each bearing step.
     * 
     * @param track
     *            The GPS track of competitor to use for bearings calculation
     * @param fromTimePoint
     *            The from time point (inclusive) for resulting bearing steps
     * @param tillTimePoint
     *            The till time point (inclusive) for resulting bearing steps
     * @param frequency
     *            Time distance between bearing time point
     * @return The list of bearings between the provided time range
     */
    public static List<BearingStep> getBearingSteps(GPSFixTrack<Competitor, GPSFixMoving> track,
            TimePoint fromTimePoint, TimePoint tillTimePoint, Duration frequency) {
        List<BearingStep> relevantBearings = new ArrayList<>();
        Bearing lastBearing = null;
        double lastCourseChangeAngleInDegrees = 0;
        for (TimePoint timePoint = fromTimePoint; !timePoint.after(tillTimePoint); timePoint = timePoint
                .plus(frequency)) {
            SpeedWithBearing estimatedSpeed = track.getEstimatedSpeed(timePoint);
            if (estimatedSpeed != null) {
                Bearing bearing = estimatedSpeed.getBearing();
                // First bearing step supposed to have 0 as course change as
                // it does not have any previous steps with bearings to compute bearing difference.
                // If the condition is not met, the existing code which uses ManeuverBearingStep class will break.
                double courseChangeAngleInDegrees = lastBearing == null ? 0
                        : lastBearing.getDifferenceTo(bearing).getDegrees();

                // In extreme cases, the getDifferenceTo() might compute a bearing in a wrong maneuver direction due to
                // fast turn and/or inaccurate GPS during penalty circles.
                // We need to ensure that our totalCourseChange does not get reduced erroneously. It is more likely to
                // have a course change step sequence
                // like 20, 70, 120, 200, 90, 20 which produces 520 degrees total course change than a sequence with 20,
                // 70, 120, -160, 90, 20 which produces 160 degrees total course change.
                // If we fail to take care of the signum, penalty circle computation will fail due to inconsistencies
                // with douglas peucker fixes.
                if (Math.abs(Math.signum(courseChangeAngleInDegrees) - Math.signum(lastCourseChangeAngleInDegrees)) == 2
                        && Math.abs(courseChangeAngleInDegrees - lastCourseChangeAngleInDegrees) >= 180) {
                    courseChangeAngleInDegrees += courseChangeAngleInDegrees < 0 ? 360 : -360;
                }
                relevantBearings.add(new BearingStep(timePoint, estimatedSpeed, courseChangeAngleInDegrees));
                lastBearing = bearing;
                lastCourseChangeAngleInDegrees = courseChangeAngleInDegrees;
            }

        }
        return relevantBearings;
    }

    /**
     * Represents a bearing step within a certain part of a GPS track. It consists of time point, speed with bearing,
     * and course change in degrees. The latter is calculated as course change between the bearing of the previous step
     * and this step. If there is no previous step, then the course change in degrees value is zero.
     * 
     * @author Vladislav Chumak (D069712)
     *
     */
    public static class BearingStep {
        private final TimePoint timePoint;
        private final SpeedWithBearing speedWithBearing;
        private final double courseChangeInDegrees;

        /**
         * Constructs a bearing step with details about speed, bearing and course change related to the previous step.
         * 
         * @param timePoint
         *            The time point when the step details have been recorded
         * @param speedWithBearing
         *            Speed with bearing at the provided time point
         * @param courseChangeInDegrees
         *            Course change in degrees compared to the previous step. Zero, if this is a first step.
         */
        public BearingStep(TimePoint timePoint, SpeedWithBearing speedWithBearing, double courseChangeInDegrees) {
            this.timePoint = timePoint;
            this.speedWithBearing = speedWithBearing;
            this.courseChangeInDegrees = courseChangeInDegrees;
        }

        public TimePoint getTimePoint() {
            return timePoint;
        }

        public SpeedWithBearing getSpeedWithBearing() {
            return speedWithBearing;
        }

        public double getCourseChangeInDegrees() {
            return courseChangeInDegrees;
        }
    }
}
