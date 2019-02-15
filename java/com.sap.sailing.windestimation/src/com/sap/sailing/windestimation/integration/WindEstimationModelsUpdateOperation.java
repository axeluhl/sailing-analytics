package com.sap.sailing.windestimation.integration;

import com.sap.sse.replication.OperationWithResult;

/**
 * 
 * Needed in the context of the initial replication of the wind estimation factory service.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimationModelsUpdateOperation
        extends OperationWithResult<WindEstimationFactoryServiceImpl, Void> {

}
