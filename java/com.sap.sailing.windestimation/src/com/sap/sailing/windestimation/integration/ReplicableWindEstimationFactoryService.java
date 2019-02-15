package com.sap.sailing.windestimation.integration;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.windestimation.WindEstimationFactoryService;
import com.sap.sse.replication.ReplicableWithObjectInputStream;

/**
 * Wind estimation factory service which supports replication on initial load (similar to {@link PolarDataService}).
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ReplicableWindEstimationFactoryService extends WindEstimationFactoryService,
        ReplicableWithObjectInputStream<WindEstimationFactoryServiceImpl, WindEstimationModelsUpdateOperation> {
}
