package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;

public interface DynamicTrackedRegatta extends TrackedRegatta {

    DynamicTrackedRace getTrackedRace(RaceDefinition race);
    
    @Override
    Iterable<DynamicTrackedRace> getTrackedRaces();

    /**
     * Non-blocking call that returns <code>null</code> if no tracking information currently exists
     * for <code>race</code>. See also {@link #getTrackedRace(RaceDefinition)} for a blocking variant.
     */
    DynamicTrackedRace getExistingTrackedRace(RaceDefinition race);
}
