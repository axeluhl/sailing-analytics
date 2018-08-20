package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sse.common.Bearing;

public class CourseChangeCalculator {
    private static final double THRESHOLD_UP_TO_WHICH_TO_ACCEPT_DIRECTION_IN_DEGREES = 90;
    
    /**
     * A maneuver is assumed to have started with course over ground {@code courseBeforeManeuver}. Up to the previous
     * fix considered, the total course change so far was {@code courseChangeInDegreesSoFar}. This method determines the
     * new total course change, starting from {@code courseBeforeManeuver} to the {@code currentCourse}, assuming that
     * generally the maneuver has been conducted by a turn towards {@code maneuverDirection}. Small direction changes
     * (<{@link #THRESHOLD_UP_TO_WHICH_TO_ACCEPT_DIRECTION_IN_DEGREES}) may happen the other way, especially at the
     * beginning of the maneuver. But when large direction changes occur, those will likely have happened in the
     * {@code maneuverDirection} and if recognized as turning the other way, the course change between fixes was likely
     * more than 180deg.
     * <p>
     * 
     * With these assumptions, the new total course change is determined and returned. In any case, adding the course
     * change returned by this method to {@code courseBeforeManeuver}, the {@code currentCourse} will result, modulo
     * 360deg.
     */
    public double getTotalCourseChange(Bearing courseBeforeManeuver, Bearing currentCourse,
            double courseChangeInDegreesSoFar, NauticalSide maneuverDirection) {
        double lastCourseInDegrees = (courseBeforeManeuver.getDegrees() + courseChangeInDegreesSoFar) % 360.;
        double diffToCurrentCourseInDegrees = currentCourse.getDegrees() % 360. - lastCourseInDegrees;
        if (Math.abs(diffToCurrentCourseInDegrees) > THRESHOLD_UP_TO_WHICH_TO_ACCEPT_DIRECTION_IN_DEGREES) {
            if (diffToCurrentCourseInDegrees < 0 && maneuverDirection == NauticalSide.STARBOARD) {
                diffToCurrentCourseInDegrees = 360.-diffToCurrentCourseInDegrees;
            } else if (diffToCurrentCourseInDegrees > 0 && maneuverDirection == NauticalSide.PORT) {
                diffToCurrentCourseInDegrees = -360.+diffToCurrentCourseInDegrees;
            }
        }
        double result = courseChangeInDegreesSoFar + diffToCurrentCourseInDegrees;
        return result;
    }
}
