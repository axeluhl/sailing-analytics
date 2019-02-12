package com.sap.sailing.windestimation.model.store;

import java.io.Serializable;

import com.sap.sailing.windestimation.model.ModelContext;

/**
 * Represents a Machine Learning model which can be persisted and loaded using {@link ModelStore}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instance for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y. For instance, to classify a maneuver type
 *            of a maneuver, a maneuver instance with features such as speed in/out, turning rate, lowest speed and etc.
 *            must be provided to maneuver classifier.
 * @param <MC>
 *            The type of model context associated with this model.
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
