package com.sap.sailing.polars;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sse.replication.impl.ReplicableWithObjectInputStream;

public interface ReplicablePolarService extends PolarDataService, ReplicableWithObjectInputStream<PolarDataService, PolarDataOperation<?>> {
}
