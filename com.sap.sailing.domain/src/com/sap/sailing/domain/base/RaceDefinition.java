package com.sap.sailing.domain.base;

public interface RaceDefinition extends Named {
    BoatClass getBoatClass();
    
    Course getCourse();

    Iterable<Competitor> getCompetitors();

    /**
     * A course may be updated while the race is on. This can have an impact on the waypoints,
     * their number and order and the buoys of which waypoints consist. This operation carefully
     * and incrementally updates the {@link Course} object returned by {@link #getCourse()}.
     * 
     * TODO need to update TrackedRace regarding the TrackedLeg and TrackedLegOfCompetitor instances
     */
    void updateCourse(Course course);
}
