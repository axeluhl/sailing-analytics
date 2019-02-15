package com.sap.sailing.windestimation.integration;

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
