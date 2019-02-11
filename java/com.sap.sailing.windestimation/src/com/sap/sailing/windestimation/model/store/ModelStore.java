package com.sap.sailing.windestimation.model.store;

import java.util.List;
import java.util.Map;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

/**
 * Persistence layer for {@link PersistableModel} which can be implemented using various persistence technologies, e.g.
 * database of file system. To comply with the persistence key generation strategy and etc., it is highly recommended to
 * sub-type {@link AbstractModelStoreImpl} if a new implementation of {@link ModelStore} is considered.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ModelStore {

    /**
     * Persists the provided model.
     * 
     * @param trainedModel
     *            The model to persist
     * @throws ModelPersistenceException
     *             In case something goes wrong
     * @see #loadModel(TrainableModel)
     */
    void persistModel(PersistableModel<?, ?> trainedModel) throws ModelPersistenceException;

    /**
     * Loads persisted model which is compatible with the provided untrained model in terms of implementation and model
     * context.
     * 
     * @param untrainedModel
     *            Not trained model for which a trained model will be determined
     * @return New instance with the loaded model, if a suitable persistent model could be found, otherwise
     *         {@code null}.
     * @throws ModelPersistenceException
     *             In case something goes wrong
     * @see #persistModel(PersistableModel)
     */
    <InstanceType, MC extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, MC>> ModelType loadModel(
            ModelType untrainedModel) throws ModelPersistenceException;

    /**
     * Deletes all persisted models which are associated with the provided model domain type
     * 
     * @param modelDomainType
     *            The domain with models to delete
     * @throws ModelPersistenceException
     *             In case something goes wrong
     */
    void deleteAll(ModelDomainType modelDomainType) throws ModelPersistenceException;

    /**
     * Exports all the models persisted in this model store which are associated with the provided model domain type.
     * The exported model can be imported by a different implementation of {@link ModelStore}.
     * 
     * @param modelDomainType
     *            Only models of this domain type will be exported
     * @return Map with persisted keys and corresponding models in its serialized state
     * @throws ModelPersistenceException
     *             In case something goes wrong
     * @see #importPersistedModels(Map, ModelDomainType)
     */
    Map<String, byte[]> exportAllPersistedModels(ModelDomainType modelDomainType) throws ModelPersistenceException;

    /**
     * Imports all the provided exported models and associates them with the provided domain type.
     * 
     * @param exportedPersistedModels
     *            The exported models which will be imported
     * @param modelDomainType
     *            The domain type to associate with the imported models
     * @throws ModelPersistenceException
     *             In case something goes wrong
     * @see {@link #exportAllPersistedModels(ModelDomainType)}
     */
    void importPersistedModels(Map<String, byte[]> exportedPersistedModels, ModelDomainType modelDomainType)
            throws ModelPersistenceException;

    /**
     * Loads all models persisted in this model store which are associated with the provided domain type.
     * 
     * @param modelDomainType
     *            Only models associated with this domain type will be loaded
     * @return A list of loaded models
     * @see #loadModel(TrainableModel)
     */
    List<PersistableModel<?, ?>> loadAllPersistedModels(ModelDomainType modelDomainType);
}
