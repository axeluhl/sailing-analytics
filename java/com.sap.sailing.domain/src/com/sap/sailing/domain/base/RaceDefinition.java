package com.sap.sailing.domain.base;

import java.io.Serializable;
import java.util.Map;

import com.sap.sailing.domain.common.RaceCompetitorIdsAsStringWithMD5Hash;
import com.sap.sse.common.NamedWithID;

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
public interface RaceDefinition extends NamedWithID {
    BoatClass getBoatClass();
    
    Course getCourse();

    Iterable<Competitor> getCompetitors();

    Iterable<Boat> getBoats();

    Competitor getCompetitorById(Serializable competitorID);

    Map<Competitor, Boat> getCompetitorsAndTheirBoats();
    
    /**
     * Gets the boat used by the competitor for this race.
     */
    Boat getBoatOfCompetitor(Competitor competitor);

    /**
     * The MD5 hash as produced by
     * {@link RaceCompetitorIdsAsStringWithMD5Hash#getMd5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID()}
     * for this race's competitor set.
     */
    byte[] getCompetitorMD5();

}
