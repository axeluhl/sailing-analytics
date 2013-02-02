package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.racelog.RaceLogStore;

public interface DynamicTrackedRegatta extends TrackedRegatta {

    DynamicTrackedRace getTrackedRace(RaceDefinition race);

    /**
     * Non-blocking call that returns <code>null</code> if no tracking information currently exists
     * for <code>race</code>. See also {@link #getTrackedRace(RaceDefinition)} for a blocking variant.
     */
    DynamicTrackedRace getExistingTrackedRace(RaceDefinition race);

    /**
     * @param raceDefinitionSetToUpdate
     *            may be <code>null</code> which means that no update will be fired to any
     *            {@link DynamicRaceDefinitionSet}.
     */
    DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, WindStore windStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, RaceLogStore raceLogStore);

}
