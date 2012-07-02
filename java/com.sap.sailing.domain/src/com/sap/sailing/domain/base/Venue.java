package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

/**
 * Describes the place where a sailing event with one or more regattas takes place.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Venue extends Named {
    Iterable<CourseArea> getCourseAreas();
    
    void addCourseArea(CourseArea courseArea);
    
    void removeCourseArea(CourseArea courseArea);
}
