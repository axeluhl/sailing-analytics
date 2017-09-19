package com.sap.sailing.server.impl;

import com.sap.sailing.domain.tracking.TrackedRegattaListener;

public interface TrackedRegattaListenerManager extends TrackedRegattaListener {

    void addListener(TrackedRegattaListener listener);

    void removeListener(TrackedRegattaListener listener);
}
