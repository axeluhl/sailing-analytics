package com.sap.sailing.server.impl;

import com.sap.sailing.domain.tracking.TrackedRegattaListener;
import com.sap.sailing.server.interfaces.RacingEventService;

/**
 * Extended version of {@link TrackedRegattaListener} used in the context of {@link RacingEventService}. That
 * encapsulates a handler manager holding registered listeners and dispatches events to those.
 */
public interface TrackedRegattaListenerManager extends TrackedRegattaListener {

    void addListener(TrackedRegattaListener listener);

    void removeListener(TrackedRegattaListener listener);
}
