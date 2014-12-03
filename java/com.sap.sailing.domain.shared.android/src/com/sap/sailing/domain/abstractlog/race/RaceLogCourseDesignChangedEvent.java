package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.base.CourseBase;

public interface RaceLogCourseDesignChangedEvent extends RaceLogEvent {

    CourseBase getCourseDesign();

}
