package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;

public interface DynamicTrackedEvent extends TrackedEvent {

    DynamicTrackedRace getTrackedRace(RaceDefinition race);

    /**
     * Non-blocking call that returns <code>null</code> if no tracking information currently exists
     * for <code>race</code>. See also {@link #getTrackedRace(RaceDefinition)} for a blocking variant.
     */
    DynamicTrackedRace getExistingTrackedRace(RaceDefinition race);
}
