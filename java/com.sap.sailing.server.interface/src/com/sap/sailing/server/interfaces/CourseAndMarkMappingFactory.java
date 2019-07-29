package com.sap.sailing.server.interfaces;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.CourseTemplateMapping;

/**
 * From a {@link CourseTemplate} constructs or updates a {@link CourseBase}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseAndMarkMappingFactory {
    Course createCourse(CourseTemplate courseTemplate, int numberOfLaps);

    void updateCourse(Course courseToUpdate, CourseTemplate courseTemplate, int numberOfLaps);

    CourseTemplateMapping createMappingForCourseTemplate(Regatta regatta, CourseTemplate courseTemplate);

    // TODO Define Course based on CourseTemplateMapping that internally references MarkTemplateMappings

    // TODO Do we need to loosely couple creation of DefineMarkEvents for the Regatta
    Course createCourseFromMappingAndDefineMarksAsNeeded(Regatta regatta, CourseTemplateMapping courseTemplateMapping,
            int numberOfLaps);

}
