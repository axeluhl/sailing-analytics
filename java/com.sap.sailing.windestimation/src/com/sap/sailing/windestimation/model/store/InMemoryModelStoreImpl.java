package com.sap.sailing.windestimation.model.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

/**
 * {@link ModelStore} which manages persistent models in-memory in a {@link ConcurrentHashMap}. This model store is
 * meant for replica instances which cannot rely on a functioning persistence layer such as MongoDB.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class InMemoryModelStoreImpl extends AbstractModelStoreImpl {

    private final Map<String, byte[]> serializedModels = new ConcurrentHashMap<>();

    @Override
    public <InstanceType, T extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadModel(
            ModelType newModel) throws ModelPersistenceException {
        ModelSerializationStrategy serializationStrategy = checkAndGetModelSerializationStrategy(newModel);
        byte[] serializedModel = serializedModels.get(getPersistenceKey(newModel));
        if (serializedModel != null) {
            try (InputStream input = new ByteArrayInputStream(serializedModel)) {
                @SuppressWarnings("unchecked")
                ModelType loadedModel = (ModelType) serializationStrategy.deserializeFromStream(input);
                if (!newModel.getModelContext().equals(loadedModel.getModelContext())) {
                    throw new ModelPersistenceException("The configuration of the loaded model is: "
                            + loadedModel.getModelContext() + ". \nExpected: " + newModel.getModelContext());
                }
                return loadedModel;
            } catch (IOException e) {
                throw new ModelPersistenceException(e);
            }
        }
        throw new ModelNotFoundException(newModel.getModelContext());
    }

    @Override
    public void persistModel(PersistableModel<?, ?> trainedModel) throws ModelPersistenceException {
        ModelSerializationStrategy serializationStrategy = checkAndGetModelSerializationStrategy(trainedModel);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            serializationStrategy.serializeToStream(trainedModel, output);
            byte[] serializedModel = output.toByteArray();
            serializedModels.put(getPersistenceKey(trainedModel), serializedModel);
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    public <T extends PersistableModel<?, ?>> void delete(T newModel) throws ModelPersistenceException {
        checkAndGetModelSerializationStrategy(newModel);
        serializedModels.remove(getPersistenceKey(newModel));
    }

    @Override
    public void deleteAll(ModelDomainType domainType) {
        for (Iterator<String> iterator = serializedModels.keySet().iterator(); iterator.hasNext();) {
            String filename = iterator.next();
            if (filename.endsWith(PERSISTENCE_KEY_SUFFIX)
                    && filename.startsWith(getPersistenceKeyPartOfModelDomain(domainType))) {
                iterator.remove();
            }
        }
    }

    @Override
    public Map<String, byte[]> exportAllPersistedModels(ModelDomainType domainType) {
        Map<String, byte[]> exportedModels = new HashMap<>();
        for (Entry<String, byte[]> entry : serializedModels.entrySet()) {
            String fileName = entry.getKey();
            if (isPersistenceKeyBelongingToModelDomain(fileName, domainType)) {
                exportedModels.put(fileName, entry.getValue());
            }
        }
        return exportedModels;
    }

    @Override
    public void importPersistedModels(Map<String, byte[]> exportedPersistedModels, ModelDomainType domainType) {
        for (Entry<String, byte[]> entry : exportedPersistedModels.entrySet()) {
            String fileName = entry.getKey();
            byte[] exportedModel = entry.getValue();
            serializedModels.put(fileName, exportedModel);
        }
    }

    @Override
    public List<PersistableModel<?, ?>> loadAllPersistedModels(ModelDomainType domainType) {
        List<PersistableModel<?, ?>> loadedModels = new ArrayList<>();
        for (Entry<String, byte[]> entry : serializedModels.entrySet()) {
            String fileName = entry.getKey();
            if (isPersistenceKeyBelongingToModelDomain(fileName, domainType)) {
                ModelSerializationStrategy serializationStrategy = getModelSerializationStrategyFromPersistenceKey(
                        fileName);
                if (serializationStrategy == null) {
                    throw new ModelLoadingException(
                            "Persistence support could not be determined due to invalid filename pattern: \"" + fileName
                                    + "\"");
                }
                PersistableModel<?, ?> loadedModel;
                try (InputStream input = new ByteArrayInputStream(entry.getValue())) {
                    loadedModel = serializationStrategy.deserializeFromStream(input);
                } catch (IOException e) {
                    throw new ModelLoadingException(
                            "Could not read model \"" + fileName + "\" from its serialized in-memory state", e);
                }
                loadedModels.add(loadedModel);
            }
        }
        return loadedModels;
    }

}
