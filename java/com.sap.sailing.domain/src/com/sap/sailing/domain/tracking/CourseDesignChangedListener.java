package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.CourseData;

public interface CourseDesignChangedListener {

    void courseDesignChanged(CourseData newCourseDesign);

}
