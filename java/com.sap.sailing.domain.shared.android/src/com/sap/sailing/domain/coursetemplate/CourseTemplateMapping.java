package com.sap.sailing.domain.coursetemplate;

import java.util.Map;

/**
 * A complete mapping of a {@link #getCourseTemplate() course template's} {@link CourseTemplate#getMarks() mark
 * templates} to {@link MarkConfiguration} objects that describe how the concrete marks are to be configured.
 * "Complete" means that the {@link Map#keySet() key set} of the map returned by {@link #getMappingsByMarkTemplate()}
 * equals the set of mark templates returned by {@link #getCourseTemplate()}.{@link CourseTemplate#getMarks()
 * getMarks()}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CourseTemplateMapping {
    CourseTemplate getCourseTemplate();

    Map<MarkTemplate, MarkConfiguration> getMappingsByMarkTemplate();
}
