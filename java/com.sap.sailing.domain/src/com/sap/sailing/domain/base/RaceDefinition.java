package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;

/**
 * Tells the {@link BoatClass boat class} and the {@link Course course} for a single race that is usually part of a
 * regatta. Note, that a course may change over time, even while the race is on, because the race committee may decide,
 * e.g., to remove a waypoint due to little wind.
 * <p>
 * 
 * A {@link Course} can be {@link Course#addCourseListener(CourseListener) observed} for waypoint additions and
 * removals.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface RaceDefinition extends Named, WithID {
    BoatClass getBoatClass();
    
    Course getCourse();

    Iterable<Competitor> getCompetitors();
}
