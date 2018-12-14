package com.sap.sailing.windestimation.model.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.sap.sailing.windestimation.classifier.ModelPersistenceException;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;

public class FileSystemModelStore implements ModelStore {

    private static final String CONTEXT_NAME_PREFIX = "modelFor";
    private final String destinationFolder;
    private static final String FILE_EXT = ".clf";

    public FileSystemModelStore(String destinationFolder, ContextType contextType) {
        this.destinationFolder = destinationFolder;
    }

    private File getFileForClassifier(PersistenceSupport trainedModel, ContextType contextType) {
        StringBuilder filePath = new StringBuilder();
        filePath.append(destinationFolder);
        filePath.append(File.separator);
        filePath.append(getContextPrefix(contextType));
        filePath.append(trainedModel.getPersistenceKey());
        filePath.append(FILE_EXT);
        String finalFilePath = replaceSystemChars(filePath.toString());
        return new File(finalFilePath);
    }

    private String replaceSystemChars(String str) {
        return str.replaceAll("[\\\\\\/\\\"\\:\\|\\<\\>\\*\\?]", "__");
    }

    @Override
    public <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        File classifierFile = getFileForClassifier(persistenceSupport,
                newModel.getContextSpecificModelMetadata().getContextType());
        if (classifierFile.exists()) {
            try (FileInputStream input = new FileInputStream(classifierFile)) {
                @SuppressWarnings("unchecked")
                ModelType loadedModel = (ModelType) persistenceSupport.loadFromStream(input);
                if (!newModel.getContextSpecificModelMetadata().equals(loadedModel.getContextSpecificModelMetadata())) {
                    throw new ModelPersistenceException("The configuration of the loaded classifier is: "
                            + loadedModel.getContextSpecificModelMetadata() + ". \nExpected: "
                            + newModel.getContextSpecificModelMetadata());
                }
                return loadedModel;
            } catch (IOException e) {
                throw new ModelPersistenceException(e);
            }
        }
        return null;
    }

    @Override
    public <T extends PersistableModel<?, ?>> void persistState(T trainedModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(trainedModel);
        try (FileOutputStream output = new FileOutputStream(getFileForClassifier(persistenceSupport,
                trainedModel.getContextSpecificModelMetadata().getContextType()))) {
            persistenceSupport.saveToStream(output);
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public <T extends PersistableModel<?, ?>> void delete(T newModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        File classifierFile = getFileForClassifier(persistenceSupport,
                newModel.getContextSpecificModelMetadata().getContextType());
        try {
            Files.deleteIfExists(classifierFile.toPath());
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public void deleteAll(ContextType contextType) throws ModelPersistenceException {
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

    private String getContextPrefix(ContextType contextType) {
        return CONTEXT_NAME_PREFIX + contextType.getContextName() + ".";
    }

}
