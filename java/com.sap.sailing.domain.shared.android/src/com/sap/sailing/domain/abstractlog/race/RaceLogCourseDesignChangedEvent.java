package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.CourseDesignerMode;

public interface RaceLogCourseDesignChangedEvent extends RaceLogEvent {

    CourseBase getCourseDesign();
    
    CourseDesignerMode getCourseDesignerMode();

}
