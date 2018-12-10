package com.sap.sailing.windestimation.classifier.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;

public class FileSystemClassifierModelStore implements ClassifierModelStore {

    private static final String CONTEXT_NAME_PREFIX = "classifiersFor";
    private final String destinationFolder;
    private static final String FILE_EXT = ".clf";

    public FileSystemClassifierModelStore(String destinationFolder, ContextType contextType) {
        this.destinationFolder = destinationFolder;
    }

    private File getFileForClassifier(PersistenceSupport<?> trainedModel, ContextType contextType) {
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
    public <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableClassificationModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ClassifierPersistenceException {
        PersistenceSupport<?> persistenceSupport = checkAndGetPersistenceSupport(newModel);
        File classifierFile = getFileForClassifier(persistenceSupport,
                newModel.getModelMetadata().getContextSpecificModelMetadata().getContextType());
        if (classifierFile.exists()) {
            try (FileInputStream input = new FileInputStream(classifierFile)) {
                @SuppressWarnings("unchecked")
                ModelType loadedModel = (ModelType) persistenceSupport.loadFromStream(input);
                if (!newModel.getModelMetadata().equals(loadedModel.getModelMetadata())) {
                    throw new ClassifierPersistenceException("The configuration of the loaded classifier is: "
                            + loadedModel.getModelMetadata() + ". \nExpected: " + newModel.getModelMetadata());
                }
                return loadedModel;
            } catch (IOException e) {
                throw new ClassifierPersistenceException(e);
            }
        }
        return null;
    }

    @Override
    public void persistState(TrainableClassificationModel<?, ?> trainedModel) throws ClassifierPersistenceException {
        PersistenceSupport<?> persistenceSupport = checkAndGetPersistenceSupport(trainedModel);
        try (FileOutputStream output = new FileOutputStream(getFileForClassifier(persistenceSupport,
                trainedModel.getModelMetadata().getContextSpecificModelMetadata().getContextType()))) {
            persistenceSupport.saveToStream(output);
        } catch (IOException e) {
            throw new ClassifierPersistenceException(e);
        }
    }

    @Override
    public void delete(TrainableClassificationModel<?, ?> newModel) throws ClassifierPersistenceException {
        PersistenceSupport<?> persistenceSupport = checkAndGetPersistenceSupport(newModel);
        File classifierFile = getFileForClassifier(persistenceSupport,
                newModel.getModelMetadata().getContextSpecificModelMetadata().getContextType());
        try {
            Files.deleteIfExists(classifierFile.toPath());
        } catch (IOException e) {
            throw new ClassifierPersistenceException(e);
        }
    }

    @Override
    public void deleteAll(ContextType contextType) throws ClassifierPersistenceException {
        File folder = new File(destinationFolder);
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(FILE_EXT)
                    && file.getName().startsWith(getContextPrefix(contextType))) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException e) {
                    throw new ClassifierPersistenceException(e);
                }
            }
        }
    }

    private String getContextPrefix(ContextType contextType) {
        return CONTEXT_NAME_PREFIX + contextType.getContextName() + ".";
    }

}
