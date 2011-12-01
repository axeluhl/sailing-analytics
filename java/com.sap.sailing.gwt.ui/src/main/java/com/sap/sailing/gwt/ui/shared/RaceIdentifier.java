package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public interface RaceIdentifier extends IsSerializable {
    Object getRace(RaceFetcher raceFetcher);

    /**
     * Blocks and waits if the tracked race for this race identifier doesn't exist yet.
     */
    Object getTrackedRace(RaceFetcher raceFetcher);

    /**
     * Immediately returns <code>null</code> if the tracked race for this race identifier doesn't exist yet.
     */
    Object getExistingTrackedRace(RaceFetcher sailingServiceImpl);
}
