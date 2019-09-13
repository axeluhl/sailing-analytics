package com.sap.sailing.windestimation.model.exception;

import com.sap.sailing.windestimation.model.ModelContext;

/**
 * Indicates that no model could be found for a model context.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
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
