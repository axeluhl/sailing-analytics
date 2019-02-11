package com.sap.sailing.windestimation.model.store;

import java.io.Serializable;

import com.sap.sailing.windestimation.model.ModelContext;

/**
 * Represents a Machine Learning model which can be persisted and loaded using {@link ModelStore}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of instances which are used as input for prediction with this model
 * @param <MC>
 *            The type of context of the model
 */
public interface PersistableModel<InstanceType, MC extends ModelContext<InstanceType>> extends Serializable {

    /**
     * Gets the persistence support type which defines the serialization/deserialization strategy for the model.
     * 
     * @see ModelSerializationStrategyType
     */
    ModelSerializationStrategyType getPersistenceSupportType();

    /**
     * Gets the associated context of the model.
     * 
     * @see ModelContext
     */
    MC getModelContext();
}
