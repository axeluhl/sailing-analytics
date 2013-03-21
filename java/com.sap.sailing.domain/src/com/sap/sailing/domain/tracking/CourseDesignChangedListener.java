package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;

import com.sap.sailing.domain.base.CourseData;

public interface CourseDesignChangedListener {

    void courseDesignChanged(CourseData newCourseDesign) throws MalformedURLException, IOException;

}
