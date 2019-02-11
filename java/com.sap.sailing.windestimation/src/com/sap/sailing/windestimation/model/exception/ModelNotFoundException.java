package com.sap.sailing.windestimation.model.exception;

import com.sap.sailing.windestimation.model.ModelContext;

public class ModelNotFoundException extends ModelPersistenceException {

    private static final long serialVersionUID = 2426127713965713226L;
    private final ModelContext<?> modelContext;

    public ModelNotFoundException(ModelContext<?> modelContext) {
        this.modelContext = modelContext;
    }

    public ModelNotFoundException(ModelContext<?> modelContext, Throwable e) {
        super(e);
        this.modelContext = modelContext;
    }

    public ModelContext<?> getModelContext() {
        return modelContext;
    }

}
