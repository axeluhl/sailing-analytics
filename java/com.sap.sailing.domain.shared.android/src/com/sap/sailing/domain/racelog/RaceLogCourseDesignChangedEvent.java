package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.base.CourseBase;

public interface RaceLogCourseDesignChangedEvent extends RaceLogEvent {

    CourseBase getCourseDesign();

}
