package com.sap.sailing.windestimation.model.exception;

import java.io.IOException;

/**
 * Indicates that something went wrong in the context of model persistence and/or serialization/deserialization.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ModelPersistenceException extends IOException {

    private static final long serialVersionUID = -7672415389872133921L;

    public ModelPersistenceException() {
    }

    public ModelPersistenceException(String message) {
        super(message);
    }

    public ModelPersistenceException(Throwable cause) {
        super(cause);
    }

    public ModelPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
