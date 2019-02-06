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

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

public class InMemoryModelStore extends AbstractModelStore {

    private final Map<String, byte[]> serializedModels = new ConcurrentHashMap<>();

    @Override
    public <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        byte[] serializedModel = serializedModels.get(getFilename(newModel));
        if (serializedModel != null) {
            try (InputStream input = new ByteArrayInputStream(serializedModel)) {
                @SuppressWarnings("unchecked")
                ModelType loadedModel = (ModelType) persistenceSupport.loadFromStream(input);
                if (!newModel.getContextSpecificModelMetadata().equals(loadedModel.getContextSpecificModelMetadata())) {
                    throw new ModelPersistenceException(
                            "The configuration of the loaded model is: " + loadedModel.getContextSpecificModelMetadata()
                                    + ". \nExpected: " + newModel.getContextSpecificModelMetadata());
                }
                return loadedModel;
            } catch (IOException e) {
                throw new ModelPersistenceException(e);
            }
        }
        throw new ModelNotFoundException(newModel.getContextSpecificModelMetadata());
    }

    @Override
    public <T extends PersistableModel<?, ?>> void persistState(T trainedModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(trainedModel);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            persistenceSupport.saveToStream(trainedModel, output);
            byte[] serializedModel = output.toByteArray();
            serializedModels.put(getFilename(trainedModel), serializedModel);
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    public <T extends PersistableModel<?, ?>> void delete(T newModel) throws ModelPersistenceException {
        checkAndGetPersistenceSupport(newModel);
        serializedModels.remove(getFilename(newModel));
    }

    @Override
    public void deleteAll(PersistenceContextType contextType) {
        for (Iterator<String> iterator = serializedModels.keySet().iterator(); iterator.hasNext();) {
            String filename = iterator.next();
            if (filename.endsWith(FILE_EXT) && filename.startsWith(getContextPrefix(contextType))) {
                iterator.remove();
            }
        }
    }

    @Override
    public Map<String, byte[]> exportAllPersistedModels(PersistenceContextType contextType) {
        Map<String, byte[]> exportedModels = new HashMap<>();
        for (Entry<String, byte[]> entry : serializedModels.entrySet()) {
            String fileName = entry.getKey();
            if (isFileBelongingToContextType(fileName, contextType)) {
                exportedModels.put(fileName, entry.getValue());
            }
        }
        return exportedModels;
    }

    @Override
    public void importPersistedModels(Map<String, byte[]> exportedPersistedModels, PersistenceContextType contextType) {
        for (Entry<String, byte[]> entry : exportedPersistedModels.entrySet()) {
            String fileName = entry.getKey();
            byte[] exportedModel = entry.getValue();
            serializedModels.put(fileName, exportedModel);
        }
    }

    @Override
    public List<PersistableModel<?, ?>> loadAllPersistedModels(PersistenceContextType contextType) {
        List<PersistableModel<?, ?>> loadedModels = new ArrayList<>();
        for (Entry<String, byte[]> entry : serializedModels.entrySet()) {
            String fileName = entry.getKey();
            if (isFileBelongingToContextType(fileName, contextType)) {
                PersistenceSupport persistenceSupport = getPersistenceSupportFromFilename(fileName);
                if (persistenceSupport == null) {
                    throw new ModelLoadingException(
                            "Persistence support could not be determined due to invalid filename pattern: \"" + fileName
                                    + "\"");
                }
                PersistableModel<?, ?> loadedModel;
                try (InputStream input = new ByteArrayInputStream(entry.getValue())) {
                    loadedModel = persistenceSupport.loadFromStream(input);
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
