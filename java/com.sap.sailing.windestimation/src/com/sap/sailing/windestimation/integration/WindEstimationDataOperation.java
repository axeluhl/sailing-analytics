package com.sap.sailing.windestimation.integration;

import com.sap.sailing.domain.windestimation.WindEstimationFactoryService;
import com.sap.sse.replication.OperationWithResult;

/**
 * 
 * Needed in the context of the initial replication of the wind estimation factory service.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 */
public interface WindEstimationDataOperation<T> extends OperationWithResult<WindEstimationFactoryService, T> {

}
