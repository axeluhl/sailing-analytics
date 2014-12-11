package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.components.FilterCriterion;

public class PolarFixFilterCriteria implements FilterCriterion<GPSFixMovingWithPolarContext> {

    @Override
    public boolean matches(GPSFixMovingWithPolarContext element) {
        boolean afterStartTime = isAfterStartTime(element);
        boolean beforeFinishTime = isBeforeFinishTime(element);
        boolean noDirectionChange = !hasDirectionChange(element);
        return (afterStartTime && beforeFinishTime && noDirectionChange);
    }

    private boolean hasDirectionChange(GPSFixMovingWithPolarContext element) {
        GPSFixTrack<Competitor, GPSFixMoving> track = element.getRace().getTrack(element.getCompetitor());
        return track.hasDirectionChange(element.getFix().getTimePoint(), element.getRace().getRace().getBoatClass()
                .getManeuverDegreeAngleThreshold());
    }

    private boolean isBeforeFinishTime(GPSFixMovingWithPolarContext element) {
        TimePoint timepointOfFix = element.getFix().getTimePoint();
        boolean isBeforeFinish;
        TimePoint finishTime = calculateFinishTime(element.getRace(), element.getCompetitor());
        isBeforeFinish = finishTime == null || timepointOfFix.before(finishTime);
        return isBeforeFinish;
    }

    private TimePoint calculateFinishTime(TrackedRace race, Competitor competitor) {
        TimePoint finishTime = race.getEndOfRace();
        if (finishTime != null) {
            Course course = race.getRace().getCourse();
            if (course.getLastWaypoint() != course.getFirstWaypoint()) {
                MarkPassing finishPassing = race.getMarkPassing(competitor, course.getLastWaypoint());
                if (finishPassing != null) {
                    TimePoint passedFinishTimePoint = finishPassing.getTimePoint();
                    if (passedFinishTimePoint.before(finishTime)) {
                        finishTime = passedFinishTimePoint;
                    }
                }
            }
        }
        return finishTime;
    }

    private boolean isAfterStartTime(GPSFixMovingWithPolarContext element) {
        TimePoint timepointOfFix = element.getFix().getTimePoint();
        boolean isAfterStart;
        try {
            TimePoint startTime = calculateStartTime(element.getRace(), element.getCompetitor());
            isAfterStart = timepointOfFix.after(startTime);
        } catch (CompetitorDidNotStartYetException exception) {
            isAfterStart = false;
        }
        return isAfterStart;
    }

    private TimePoint calculateStartTime(TrackedRace race, Competitor competitor) throws CompetitorDidNotStartYetException {
        MarkPassing startPassing = race.getMarkPassing(competitor, race.getRace().getCourse().getFirstWaypoint());
        if (startPassing == null) {
            throw new CompetitorDidNotStartYetException();
        }
        TimePoint raceStartTime = race.getStartOfRace();
        TimePoint startTime;
        TimePoint passedStartTimePoint = startPassing.getTimePoint();
        if (passedStartTimePoint.after(raceStartTime)) {
            startTime = passedStartTimePoint;
        } else {
            startTime = raceStartTime;
        }
        return startTime;
    }
    
    private class CompetitorDidNotStartYetException extends Exception {
        private static final long serialVersionUID = 7906688735433666009L;
    }

    @Override
    public Class<GPSFixMovingWithPolarContext> getElementType() {
        return GPSFixMovingWithPolarContext.class;
    }

}
