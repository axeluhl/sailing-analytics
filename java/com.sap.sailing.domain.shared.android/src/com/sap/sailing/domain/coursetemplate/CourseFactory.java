package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.CourseBase;

/**
 * From a {@link CourseTemplate} constructs or updates a {@link CourseBase}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseFactory {
    CourseBase createCourse(CourseTemplate courseTemplate, int numberOfLaps);

    void updateCourse(CourseBase courseToUpdate, CourseTemplate courseTemplate, int numberOfLaps);
}
