package com.sap.sailing.domain.lifecycle.impl;

import com.sap.sailing.domain.lifecycle.WithLifecycle;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Lifecycle specific for a {@link TrackedRace}. Uses a {@link TrackedRaceState} for state definitions.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 28, 2013
 */
public class TrackedRaceLifecycle extends LifecycleImpl {

    public TrackedRaceLifecycle(WithLifecycle observer) {
        super(observer);
        this.state = TrackedRaceState.INITIAL;
    }

}
