package com.sap.sailing.windestimation.model.store;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

/**
 * Base class for {@link ModelStore} implementation which contains common strategies for persistence key derivation and
 * util methods.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractModelStoreImpl implements ModelStore {

    private static final String NAME_PART_DELIMITER = ".";

    /**
     * 
     */
    protected static final String CONTEXT_NAME_PREFIX = "modelFor";

    /**
     * File extension for a model file
     */
    protected static final String FILE_EXT = ".clf";

    protected <T extends PersistableModel<?, ?>> ModelSerializationStrategy checkAndGetPersistenceSupport(
            T trainedModel) throws ModelPersistenceException {
        ModelSerializationStrategy modelSerializationStrategy = trainedModel.getPersistenceSupportType()
                .getModelSerializationStrategy();
        if (modelSerializationStrategy == null) {
            throw new ModelPersistenceException(
                    "Model of type " + trainedModel.getClass().getSimpleName() + " has no serialization strategy");
        }
        return modelSerializationStrategy;
    }

    /**
     * Gets the persistence key for the provided model. The persistence key is used as an unique identifier for the
     * provided model. The unique identifier can be considered either as a filename, or as id attribute within a
     * database. This method is idempotent.
     * 
     * @param persistableModel
     *            The model for which the persisted key will be derived
     * @return Persistence key for the provided model
     */
    protected String getPersistenceKey(PersistableModel<?, ?> persistableModel) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(getPersistenceKeyPartOfModelSerializationType(persistableModel.getPersistenceSupportType()));
        ModelDomainType domainType = persistableModel.getModelContext().getContextType();
        fileName.append(getPersistenceKeyPartOfModelDomain(domainType));
        fileName.append(persistableModel.getClass().getSimpleName());
        fileName.append(NAME_PART_DELIMITER);
        fileName.append(persistableModel.getModelContext().getId());
        fileName.append(FILE_EXT);
        String finalFileName = replaceSystemChars(fileName.toString());
        return finalFileName;
    }

    /**
     * Replaces problematic characters of provided string with doubled underscore.
     */
    private String replaceSystemChars(String str) {
        return str.replaceAll("[\\\\\\/\\\"\\:\\|\\<\\>\\*\\?]", "__");
    }

    protected String getPersistenceKeyPartOfModelDomain(ModelDomainType modelDomainType) {
        return CONTEXT_NAME_PREFIX + modelDomainType.getDomainName() + NAME_PART_DELIMITER;
    }

    protected String getPersistenceKeyPartOfModelSerializationType(
            ModelSerializationStrategyType modelSerializationStrategyType) {
        return modelSerializationStrategyType.name() + NAME_PART_DELIMITER;
    }

    protected ModelSerializationStrategy getModelSerializationStrategyFromPersistenceKey(String persistenceKey) {
        for (ModelSerializationStrategyType serializationType : ModelSerializationStrategyType.values()) {
            if (serializationType != ModelSerializationStrategyType.NONE
                    && persistenceKey.startsWith(getPersistenceKeyPartOfModelSerializationType(serializationType))) {
                return serializationType.getModelSerializationStrategy();
            }
        }
        return null;
    }

    protected boolean isPersistenceKeyBelongingToModelDomain(String persistenceKey, ModelDomainType modelDomainType) {
        return persistenceKey.endsWith(FILE_EXT)
                && persistenceKey.substring(persistenceKey.indexOf(NAME_PART_DELIMITER))
                        .startsWith(NAME_PART_DELIMITER + getPersistenceKeyPartOfModelDomain(modelDomainType));
    }

    protected <InstanceType> void verifyRequestedModelContextIsLoaded(ModelContext<?> requestedModelContext,
            ModelContext<InstanceType> loadedModelContext) throws ModelPersistenceException {
        if (!requestedModelContext.equals(loadedModelContext)) {
            throw new ModelPersistenceException("The context of the loaded model is: " + loadedModelContext
                    + ". \nExpected: " + requestedModelContext);
        }
    }

}
