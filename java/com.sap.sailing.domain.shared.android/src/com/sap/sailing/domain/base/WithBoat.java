package com.sap.sailing.domain.base;

import com.sap.sse.datamining.annotations.Connector;

/**
 * Something with a boat.
 */
public interface WithBoat {
    @Connector(messageKey = "Boat", ordinal = 10)
    Boat getBoat();
}
