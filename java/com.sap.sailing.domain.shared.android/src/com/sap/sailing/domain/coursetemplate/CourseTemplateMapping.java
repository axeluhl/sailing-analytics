package com.sap.sailing.domain.coursetemplate;

import java.util.Map;

public interface CourseTemplateMapping {
    
    CourseTemplate getCourseTemplate();

    Map<MarkTemplate, MarkTemplateMapping> getMappingsByMarkTemplate();
}
