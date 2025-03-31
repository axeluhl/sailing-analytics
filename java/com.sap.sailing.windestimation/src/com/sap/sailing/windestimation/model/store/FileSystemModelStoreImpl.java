package com.sap.sailing.windestimation.model.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

/**
 * {@link ModelStore} which manages its persistence with files on a file system.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class FileSystemModelStoreImpl extends AbstractModelStoreImpl {

    private final String destinationFolder;

    /**
     * Constructs a new instance of file system model store.
     * 
     * @param destinationFolder
     *            The folder where all models will be managed
     */
    public FileSystemModelStoreImpl(String destinationFolder) {
        this.destinationFolder = destinationFolder;
        File folder = new File(destinationFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    private File getFileForModel(PersistableModel<?, ?> persistableModel) {
        return getFileForModel(getPersistenceKey(persistableModel));
    }

    private File getFileForModel(String filename) {
        StringBuilder filePath = new StringBuilder();
        filePath.append(destinationFolder);
        filePath.append(File.separator);
        filePath.append(filename);
        String finalFilePath = filePath.toString();
        return new File(finalFilePath);
    }

    @Override
    public <InstanceType, T extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadModel(
            ModelType untrainedModel) throws ModelPersistenceException {
        ModelSerializationStrategy serializationStrategy = checkAndGetModelSerializationStrategy(untrainedModel);
        File modelFile = getFileForModel(untrainedModel);
        if (modelFile.exists()) {
            try (FileInputStream input = new FileInputStream(modelFile)) {
                @SuppressWarnings("unchecked")
                ModelType loadedModel = (ModelType) serializationStrategy.deserializeFromStream(input);
                if (!untrainedModel.getModelContext().equals(loadedModel.getModelContext())) {
                    throw new ModelPersistenceException("The configuration of the loaded model is: "
                            + loadedModel.getModelContext() + ". \nExpected: " + untrainedModel.getModelContext());
                }
                return loadedModel;
            } catch (IOException e) {
                throw new ModelPersistenceException(e);
            }
        }
        throw new ModelNotFoundException(untrainedModel.getModelContext());
    }

    @Override
    public void persistModel(PersistableModel<?, ?> trainedModel) throws ModelPersistenceException {
        ModelSerializationStrategy serializationStrategy = checkAndGetModelSerializationStrategy(trainedModel);
        try (FileOutputStream output = new FileOutputStream(getFileForModel(trainedModel))) {
            serializationStrategy.serializeToStream(trainedModel, output);
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    public <T extends PersistableModel<?, ?>> void delete(T newModel) throws ModelPersistenceException {
        checkAndGetModelSerializationStrategy(newModel);
        File modelFile = getFileForModel(newModel);
        try {
            Files.deleteIfExists(modelFile.toPath());
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public void deleteAll(ModelDomainType domainType) throws ModelPersistenceException {
        File folder = new File(destinationFolder);
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(PERSISTENCE_KEY_SUFFIX)
                    && file.getName().startsWith(getPersistenceKeyPartOfModelDomain(domainType))) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException e) {
                    throw new ModelPersistenceException(e);
                }
            }
        }
    }

    @Override
    public Map<String, byte[]> exportAllPersistedModels(ModelDomainType domainType) throws ModelPersistenceException {
        Map<String, byte[]> exportedModels = new HashMap<>();
        File folder = new File(destinationFolder);
        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            if (file.isFile() && isPersistenceKeyBelongingToModelDomain(fileName, domainType)) {
                byte[] exportedModel;
                try {
                    exportedModel = Files.readAllBytes(file.toPath());
                } catch (IOException e) {
                    throw new ModelPersistenceException("Could not read model \"" + fileName + "\" from filesystem", e);
                }
                exportedModels.put(fileName, exportedModel);
            }
        }
        return exportedModels;
    }

    @Override
    public void importPersistedModels(Map<String, byte[]> exportedPersistedModels, ModelDomainType domainType)
            throws ModelPersistenceException {
        for (Entry<String, byte[]> entry : exportedPersistedModels.entrySet()) {
            String fileName = entry.getKey();
            byte[] exportedModel = entry.getValue();
            Path modelPath = Paths.get(destinationFolder, fileName);
            try {
                Files.write(modelPath, exportedModel);
            } catch (IOException e) {
                throw new ModelPersistenceException("Could not store model \"" + fileName + "\" on filesystem", e);
            }
        }
    }

    @Override
    public List<PersistableModel<?, ?>> loadAllPersistedModels(ModelDomainType domainType) {
        List<PersistableModel<?, ?>> loadedModels = new ArrayList<>();
        File folder = new File(destinationFolder);
        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            if (file.isFile() && isPersistenceKeyBelongingToModelDomain(fileName, domainType)) {
                ModelSerializationStrategy serializationStrategy = getModelSerializationStrategyFromPersistenceKey(
                        fileName);
                if (serializationStrategy == null) {
                    throw new ModelLoadingException(
                            "Persistence support could not be determined due to invalid filename pattern: \"" + fileName
                                    + "\"");
                }
                PersistableModel<?, ?> loadedModel;
                File modelFile = getFileForModel(fileName);
                try (FileInputStream input = new FileInputStream(modelFile)) {
                    loadedModel = serializationStrategy.deserializeFromStream(input);
                } catch (IOException e) {
                    throw new ModelLoadingException("Could not read model \"" + fileName + "\" from filesystem", e);
                }
                loadedModels.add(loadedModel);
            }
        }
        return loadedModels;
    }

}
