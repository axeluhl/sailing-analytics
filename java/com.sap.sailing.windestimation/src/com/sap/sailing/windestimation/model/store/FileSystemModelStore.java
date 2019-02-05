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

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

public class FileSystemModelStore extends AbstractModelStore {

    private final String destinationFolder;

    public FileSystemModelStore(String destinationFolder) {
        this.destinationFolder = destinationFolder;
        File folder = new File(destinationFolder);
        if(!folder.exists()) {
            folder.mkdir();
        }
    }

    private File getFileForModel(PersistableModel<?, ?> persistableModel) {
        return getFileForModel(getFilename(persistableModel));
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
    public <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        File modelFile = getFileForModel(newModel);
        if (modelFile.exists()) {
            try (FileInputStream input = new FileInputStream(modelFile)) {
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
        try (FileOutputStream output = new FileOutputStream(getFileForModel(trainedModel))) {
            persistenceSupport.saveToStream(trainedModel, output);
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    public <T extends PersistableModel<?, ?>> void delete(T newModel) throws ModelPersistenceException {
        checkAndGetPersistenceSupport(newModel);
        File modelFile = getFileForModel(newModel);
        try {
            Files.deleteIfExists(modelFile.toPath());
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public void deleteAll(PersistenceContextType contextType) throws ModelPersistenceException {
        File folder = new File(destinationFolder);
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(FILE_EXT)
                    && file.getName().startsWith(getContextPrefix(contextType))) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException e) {
                    throw new ModelPersistenceException(e);
                }
            }
        }
    }

    @Override
    public Map<String, byte[]> exportAllPersistedModels(PersistenceContextType contextType)
            throws ModelPersistenceException {
        Map<String, byte[]> exportedModels = new HashMap<>();
        File folder = new File(destinationFolder);
        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            if (file.isFile() && isFileBelongingToContextType(fileName, contextType)) {
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
    public void importPersistedModels(Map<String, byte[]> exportedPersistedModels, PersistenceContextType contextType)
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
    public List<PersistableModel<?, ?>> loadAllPersistedModels(PersistenceContextType contextType) {
        List<PersistableModel<?, ?>> loadedModels = new ArrayList<>();
        File folder = new File(destinationFolder);
        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            if (file.isFile() && isFileBelongingToContextType(fileName, contextType)) {
                PersistenceSupport persistenceSupport = getPersistenceSupportFromFilename(fileName);
                if (persistenceSupport == null) {
                    throw new ModelLoadingException(
                            "Persistence support could not be determined due to invalid filename pattern: \"" + fileName
                                    + "\"");
                }
                PersistableModel<?, ?> loadedModel;
                File modelFile = getFileForModel(fileName);
                try (FileInputStream input = new FileInputStream(modelFile)) {
                    loadedModel = persistenceSupport.loadFromStream(input);
                } catch (IOException e) {
                    throw new ModelLoadingException("Could not read model \"" + fileName + "\" from filesystem", e);
                }
                loadedModels.add(loadedModel);
            }
        }
        return loadedModels;
    }

}
