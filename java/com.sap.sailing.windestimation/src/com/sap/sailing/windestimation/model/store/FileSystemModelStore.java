package com.sap.sailing.windestimation.model.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

public class FileSystemModelStore implements ModelStore {

    private static final String CONTEXT_NAME_PREFIX = "modelFor";
    private final String destinationFolder;
    private static final String FILE_EXT = ".clf";

    public FileSystemModelStore(String destinationFolder, PersistenceContextType contextType) {
        this.destinationFolder = destinationFolder;
    }

    private File getFileForModel(PersistenceSupport trainedModel, PersistenceContextType contextType) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(getContextPrefix(contextType));
        fileName.append(trainedModel.getPersistenceKey());
        fileName.append(FILE_EXT);
        String finalFileName = replaceSystemChars(fileName.toString());
        return getFileForModel(finalFileName, contextType);
    }

    private File getFileForModel(String fileName, PersistenceContextType contextType) {
        StringBuilder filePath = new StringBuilder();
        filePath.append(destinationFolder);
        filePath.append(File.separator);
        filePath.append(fileName);
        String finalFilePath = filePath.toString();
        return new File(finalFilePath);
    }

    private String replaceSystemChars(String str) {
        return str.replaceAll("[\\\\\\/\\\"\\:\\|\\<\\>\\*\\?]", "__");
    }

    @Override
    public <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        File modelFile = getFileForModel(persistenceSupport,
                newModel.getContextSpecificModelMetadata().getContextType());
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
        try (FileOutputStream output = new FileOutputStream(
                getFileForModel(persistenceSupport, trainedModel.getContextSpecificModelMetadata().getContextType()))) {
            persistenceSupport.saveToStream(output);
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    public <T extends PersistableModel<?, ?>> void delete(T newModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        File modelFile = getFileForModel(persistenceSupport,
                newModel.getContextSpecificModelMetadata().getContextType());
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

    private String getContextPrefix(PersistenceContextType contextType) {
        return CONTEXT_NAME_PREFIX + contextType.getContextName() + ".";
    }

    @Override
    public Map<String, byte[]> exportAllPersistedModels(PersistenceContextType contextType)
            throws ModelPersistenceException {
        Map<String, byte[]> exportedModels = new HashMap<>();
        File folder = new File(destinationFolder);
        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            if (file.isFile() && fileName.endsWith(FILE_EXT) && fileName.startsWith(getContextPrefix(contextType))) {
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

}
