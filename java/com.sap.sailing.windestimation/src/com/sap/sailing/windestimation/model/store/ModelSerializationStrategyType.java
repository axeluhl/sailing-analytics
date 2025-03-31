package com.sap.sailing.windestimation.model.store;

/**
 * Defines all possible serialization strategies for all implementations of {@link PersistableModel}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum ModelSerializationStrategyType {
    SERIALIZATION(new JavaSerializationBasedModelSerializationImpl()), NONE(null);

    private final ModelSerializationStrategy modelSerializationStrategy;

    private ModelSerializationStrategyType(ModelSerializationStrategy modelSerializationStrategy) {
        this.modelSerializationStrategy = modelSerializationStrategy;
    }

    public ModelSerializationStrategy getModelSerializationStrategy() {
        return modelSerializationStrategy;
    }
}
