package com.sap.sailing.domain.coursetemplate;

import java.util.Map;

/**
 * A complete mapping of a {@link #getCourseTemplate() course template's} {@link CourseTemplate#getMarks() mark
 * templates} to {@link MarkConfiguration} objects that describe how the concrete marks are to be configured.
 * "Complete" means that the {@link Map#keySet() key set} of the map returned by {@link #getMarkConfigurationsByMarkTemplate()}
 * equals the set of mark templates returned by {@link #getCourseTemplate()}.{@link CourseTemplate#getMarks()
 * getMarks()}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CourseTemplateConfiguration {
    CourseTemplate getCourseTemplate();

    Map<MarkTemplate, MarkConfiguration> getMarkConfigurationsByMarkTemplate();

    /**
     * Maps all {@link MarkTemplate}s {@link CourseTemplate#getMarks() listed by the course template} to
     * {@link MarkConfiguration}s through the result of {@link #getMarkConfigurationsByMarkTemplate()}.
     * <p>
     * 
     * Furthermore, uses the {@link CourseTemplate#getWaypoints(int)} to obtain a sequence of {@link WaypointTemplate}s
     * for the given {@code numberOfLaps} and produces a corresponding {@link WaypointWithMarkConfiguration} object for
     * each of them by mapping the {@link WaypointTemplate#getControlPoint() WaypointTemplate's control point's}
     * {@link MarkTemplate}s to {@link MarkConfiguration}s through the result of
     * {@link #getMarkConfigurationsByMarkTemplate()}. Invoking {@link CourseWithMarkConfiguration#getWaypoints()}
     * on this method's result returns those {@link WaypointWithMarkConfiguration} objects. 
     */
    CourseWithMarkConfiguration createCourseTemplateConfigurationFromMapping();
}
