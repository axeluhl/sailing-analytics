package com.sap.sailing.windestimation.integration;

import com.sap.sse.replication.OperationWithResult;

/**
 * Updates wind estimation models with provided exported models.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationModelsUpdateOperation
        implements OperationWithResult<ReplicableWindEstimationFactoryService, Void> {

    private static final long serialVersionUID = 6407877837605630090L;

    private final ExportedModels exportedModels;

    public WindEstimationModelsUpdateOperation(ExportedModels exportedModels) {
        this.exportedModels = exportedModels;
    }

    @Override
    public Void internalApplyTo(ReplicableWindEstimationFactoryService toState) throws Exception {
        toState.updateWindEstimationModels(exportedModels);
        return null;
    }

}
