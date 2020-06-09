package com.sap.sailing.shared.persistence.impl;

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;

public enum CollectionNames {
    /** Contains the {@link MarkProperties}. */
    MARK_PROPERTIES,

    /** Contains the {@link MarkTemplate} objects. */
    MARK_TEMPLATES,
    
    /** Contains the {@link MarkRole} objects. */
    MARK_ROLES,

    /** Contains the {@link CourseTemplate}s. */
    COURSE_TEMPLATES;
}
