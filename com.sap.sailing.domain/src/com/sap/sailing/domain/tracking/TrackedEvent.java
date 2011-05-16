package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.RaceDefinition;

public interface TrackedEvent {
    Event getEvent();

    Iterable<TrackedRace> getTrackedRaces();

    Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass);

    TrackedRace getTrackedRace(RaceDefinition race);

    void addTrackedRace(TrackedRace trackedRace);

    /**
     * Buoy positions, also for those defining a {@link Gate}, are variable over time.
     * Their positions are tracked.
     */
    Track<Buoy, GPSFix> getTrack(Buoy buoy);
}