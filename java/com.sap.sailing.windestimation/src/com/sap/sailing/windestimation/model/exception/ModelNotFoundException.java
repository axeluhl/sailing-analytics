package com.sap.sailing.windestimation.model.exception;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public class ModelNotFoundException extends ModelPersistenceException {

    private static final long serialVersionUID = 2426127713965713226L;
    private final ContextSpecificModelMetadata<?> modelMetadata;

    public ModelNotFoundException(ContextSpecificModelMetadata<?> modelMetadata) {
        this.modelMetadata = modelMetadata;
    }
    
    public ModelNotFoundException(ContextSpecificModelMetadata<?> modelMetadata, Throwable e) {
        super(e);
        this.modelMetadata = modelMetadata;
    }

    public ContextSpecificModelMetadata<?> getModelMetadata() {
        return modelMetadata;
    }

}
