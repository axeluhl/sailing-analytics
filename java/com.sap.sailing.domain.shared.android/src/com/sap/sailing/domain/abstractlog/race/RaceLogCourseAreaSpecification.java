package com.sap.sailing.domain.abstractlog.race;

import java.util.UUID;

import com.sap.sailing.domain.base.CourseArea;

/**
 * Specifies a {@link CourseArea course area} by its UUID
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceLogCourseAreaSpecification {
    UUID getCourseAreaId();
}
