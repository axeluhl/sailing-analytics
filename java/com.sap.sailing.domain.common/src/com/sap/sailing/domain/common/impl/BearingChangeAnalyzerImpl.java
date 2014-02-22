package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.BearingChangeAnalyzer;

public class BearingChangeAnalyzerImpl implements BearingChangeAnalyzer {

    @Override
    public boolean didPass(Bearing courseBeforeManeuver, double totalCourseChangeInDegrees, Bearing courseAfterManeuver,
            Bearing wasThisCourseReachedAndCrossedDuringManeuver) {
        final boolean result;
        Bearing diffToCourseInQuestion = courseBeforeManeuver.getDifferenceTo(wasThisCourseReachedAndCrossedDuringManeuver);
        if (Math.signum(diffToCourseInQuestion.getDegrees()) == Math.signum(totalCourseChangeInDegrees)) {
            // simple case: the actual course change was greater than what was required in that direction to
            // reach the course in question
            result = Math.abs(diffToCourseInQuestion.getDegrees()) <= Math.abs(totalCourseChangeInDegrees);
        } else {
            // more complicated: the smallest possible course change required may have been to a different direction than the
            // actual course change. The course in question may still have been reached and crossed in the actual course
            // change that points the other way has been great enough. Example: courseBeforeManeuver=355deg;
            // totalCourseChangeInDegrees=-357; courseAfterManeuver=358deg; wasThisCourseReachedAndCrossedDuringManeuver=000deg.
            // The answer has to be TRUE because the course changed to port and was almost a full circle, passing 000deg.
            // Still, the diffToCourseInQuestion is 005deg and positive.
            // Approach: revert the course change required; revert 005deg to 355deg and check again with this reverted course change.
            Bearing revertedDiffToCourseInQuestion = new DegreeBearingImpl(360 * Math.signum(diffToCourseInQuestion
                    .getDegrees())).add(diffToCourseInQuestion);
            result = Math.abs(revertedDiffToCourseInQuestion.getDegrees()) <= Math.abs(totalCourseChangeInDegrees);
        }
        return result;
    }

}
