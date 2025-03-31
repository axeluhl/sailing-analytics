package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.CourseDesignerMode;

/**
 * A course design can be defined in the {@link RaceLog} using a {@link RaceLogCourseDesignChangedEvent}. These
 * events may carry information about the course designer that was used to create the course. Not all of them
 * support defining the waypoint sequence that sets the course. See {@link RaceLogCourseDesignChangedEvent#getCourseDesignerMode()}
 * and {@link CourseDesignerMode#isWaypointSequenceValid()}. Callers can tell whether they want to obtain {@link CourseBase}
 * objects regardless of whether their waypoint sequence is valid or not.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LastPublishedCourseDesignFinder extends RaceLogAnalyzer<CourseBase> {

    private final boolean onlyCoursesWithValidWaypointList;

    /**
     * @param onlyCoursesWithValidWaypointList
     *            if {@code true}, only {@link RaceLogCourseDesignChangedEvent} objects will be considered whose
     *            {@link RaceLogCourseDesignChangedEvent#getCourseDesignerMode() course designer mode}
     *            {@link CourseDesignerMode#isWaypointSequenceValid() guarantees} that the waypoint list is valid and
     *            shall be applied to a race; otherwise, all other {@link RaceLogCourseDesignChangedEvent} as considered
     *            as well.
     */
    public LastPublishedCourseDesignFinder(RaceLog raceLog, boolean onlyCoursesWithValidWaypointList) {
        super(raceLog);
        this.onlyCoursesWithValidWaypointList = onlyCoursesWithValidWaypointList;
    }

    @Override
    protected CourseBase performAnalysis() {
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event instanceof RaceLogCourseDesignChangedEvent) {
                RaceLogCourseDesignChangedEvent courseDesignEvent = (RaceLogCourseDesignChangedEvent) event;
                if (!onlyCoursesWithValidWaypointList || (courseDesignEvent.getCourseDesignerMode() == null ||
                        courseDesignEvent.getCourseDesignerMode().isWaypointSequenceValid())) {
                    return courseDesignEvent.getCourseDesign();
                }
            }
        }
        return null;
    }
}
