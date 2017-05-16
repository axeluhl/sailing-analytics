package com.sap.sailing.polars;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sse.replication.impl.ReplicableWithObjectInputStream;

public interface ReplicablePolarService extends PolarDataService, ReplicableWithObjectInputStream<PolarDataService, PolarDataOperation<?>> {
    /**
     * Should something go wrong during initial load, using this method a caller can reset this service into a fresh,
     * empty and valid state.
     */
    void resetState();
}
