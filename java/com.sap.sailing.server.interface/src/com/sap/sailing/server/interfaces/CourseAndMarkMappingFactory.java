package com.sap.sailing.server.interfaces;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.CourseTemplateMapping;
import com.sap.sailing.domain.coursetemplate.CourseWithMarkTemplateMappings;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;

/**
 * From a {@link CourseTemplate} constructs or updates a {@link CourseBase}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseAndMarkMappingFactory {
    Course createCourse(CourseTemplate courseTemplate, int numberOfLaps);

    /**
     * The given {@link Course} need to exactly match the structure of the given {@link CourseTemplate} regarding the following specifics:
     * <ul>
     *  <li>All used {@link Mark Marks} need to reference a {@link MarkTemplate} being part of the given {@link CourseTemplate}</li>
     *  <li>The parts before and after the repeatable part need to exactly match the start and end of the course</li>
     *  <li>Between the start end only full cycles of the repeatable part need to exist</li>
     * </ul>
     * 
     * In addition, the given {@link CourseTemplate} needs to provide a repeatable part.
     * 
     */
    void updateCourse(Course courseToUpdate, CourseTemplate courseTemplate, int numberOfLaps);

    /**
     * {@link Course Courses} can optionally reference a {@link CourseTemplate} they were created from. This method can
     * be used to obtain the associated {@link CourseTemplate} for a given {@link Course}. Due to the fact that
     * {@link CourseTemplate} can be deleted without updating the {@link Course}, references may be stale. In this case,
     * the reference is handled equally to {@link Course Courses} without a reference.
     * 
     * @return the {@link CourseTemplate} associated to the given {@link Course} or null if none is associated or a
     *         reference could not be resolved.
     */
    CourseTemplate resolveCourseTemplate(Course course);

    CourseTemplateMapping createMappingForCourseTemplate(Regatta regatta, CourseTemplate courseTemplate);

    CourseWithMarkTemplateMappings createCourseTemplateMappingFromMapping(Regatta regatta,
            CourseTemplateMapping courseTemplateMapping, int numberOfLaps);

    // TODO Do we need to loosely couple creation of DefineMarkEvents for the Regatta
    Course createCourseFromMappingAndDefineMarksAsNeeded(Regatta regatta,
            CourseWithMarkTemplateMappings courseTemplateMappingWithMarkTemplateMappings);

}
