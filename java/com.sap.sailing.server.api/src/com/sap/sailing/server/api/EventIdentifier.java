package com.sap.sailing.server.api;

import java.io.Serializable;

public interface EventIdentifier extends Serializable {
    Object getEvent(EventFetcher eventFetcher);
}
