package com.sap.sailing.windestimation.integration;

import com.sap.sailing.domain.windestimation.WindEstimationFactoryService;
import com.sap.sse.replication.ReplicableWithObjectInputStream;

public interface ReplicableWindEstimationFactoryService extends WindEstimationFactoryService,
        ReplicableWithObjectInputStream<WindEstimationFactoryService, WindEstimationDataOperation<?>> {
}
