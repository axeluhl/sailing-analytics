package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.base.CourseData;

public interface RaceLogCourseDesignChangedEvent extends RaceLogEvent {

    CourseData getCourseDesign();

}
