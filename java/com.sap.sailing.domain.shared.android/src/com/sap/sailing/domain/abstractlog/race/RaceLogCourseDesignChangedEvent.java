package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.CourseDesignerMode;

/**
 * No need to implement {@link InvalidatesLeaderboardCache} as changing the course design will lead to
 * waypoint additions and removals, both of which trigger a cache invalidation already.
 */
public interface RaceLogCourseDesignChangedEvent extends RaceLogEvent {

    CourseBase getCourseDesign();
    
    CourseDesignerMode getCourseDesignerMode();

}
