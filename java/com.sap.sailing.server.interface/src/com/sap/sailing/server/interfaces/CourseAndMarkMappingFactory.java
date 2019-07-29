package com.sap.sailing.server.interfaces;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;

/**
 * From a {@link CourseTemplate} constructs or updates a {@link CourseBase}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseAndMarkMappingFactory {
    Course createCourse(CourseTemplate courseTemplate, int numberOfLaps);

    void updateCourse(Course courseToUpdate, CourseTemplate courseTemplate, int numberOfLaps);
}
