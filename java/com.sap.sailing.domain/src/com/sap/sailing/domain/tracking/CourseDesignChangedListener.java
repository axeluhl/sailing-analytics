package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;

import com.sap.sailing.domain.base.CourseBase;


public interface CourseDesignChangedListener {

    void courseDesignChanged(CourseBase newCourseDesign) throws MalformedURLException, IOException;

}
