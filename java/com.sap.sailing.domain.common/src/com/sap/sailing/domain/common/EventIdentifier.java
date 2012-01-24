package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface EventIdentifier extends Serializable {
    Object getEvent(EventFetcher eventFetcher);
}
