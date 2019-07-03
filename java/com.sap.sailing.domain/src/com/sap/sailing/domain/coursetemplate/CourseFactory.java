package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.Course;

/**
 * From a {@link CourseTemplate} constructs or updates a {@link Course}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseFactory {
    Course createCourse(CourseTemplate courseTemplate, int numberOfLaps);

    void updateCourse(Course courseToUpdate, CourseTemplate courseTemplate, int numberOfLaps);
}
