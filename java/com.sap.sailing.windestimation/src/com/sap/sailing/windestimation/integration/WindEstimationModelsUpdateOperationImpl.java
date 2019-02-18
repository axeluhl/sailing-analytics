package com.sap.sailing.windestimation.integration;

/**
 * Updates wind estimation models with provided exported models.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationModelsUpdateOperationImpl implements WindEstimationModelsUpdateOperation {

    private static final long serialVersionUID = 6407877837605630090L;

    private final ExportedModels exportedModels;

    public WindEstimationModelsUpdateOperationImpl(ExportedModels exportedModels) {
        this.exportedModels = exportedModels;
    }

    @Override
    public Void internalApplyTo(WindEstimationFactoryServiceImpl toState) throws Exception {
        toState.updateWindEstimationModels(exportedModels);
        return null;
    }

}
