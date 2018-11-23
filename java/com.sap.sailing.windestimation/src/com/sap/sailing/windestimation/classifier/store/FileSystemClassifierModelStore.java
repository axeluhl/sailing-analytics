package com.sap.sailing.windestimation.classifier.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.ModelMetadata;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;

public class FileSystemClassifierModelStore implements ClassifierModelStore {

    private final String destinationFolder;
    private static final String FILE_EXT = ".clf";

    public FileSystemClassifierModelStore(String destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    private File getFileForClassifier(PersistenceSupport trainedModel) {
        StringBuilder filePath = new StringBuilder();
        filePath.append(destinationFolder);
        filePath.append(File.separator);
        filePath.append(trainedModel.getPersistenceKey());
        filePath.append(FILE_EXT);
        String finalFilePath = replaceSystemChars(filePath.toString());
        return new File(finalFilePath);
    }

    private String replaceSystemChars(String str) {
        return str.replaceAll("[\\\\\\/\\\"\\:\\|\\<\\>\\*\\?]", "__");

    }

    @Override
    public boolean loadPersistedState(TrainableClassificationModel<?, ?> newModel)
            throws ClassifierPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        File classifierFile = getFileForClassifier(persistenceSupport);
        if (classifierFile.exists()) {
            try (FileInputStream input = new FileInputStream(classifierFile)) {
                ModelMetadata<?, ?> loadedModelMetadata = persistenceSupport.loadFromStream(input);
                if (!newModel.getModelMetadata().equals(loadedModelMetadata)) {
                    throw new ClassifierPersistenceException("The configuration of the loaded classifier is: "
                            + loadedModelMetadata + ". \nExpected: " + newModel.getModelMetadata());
                }
                return true;
            } catch (IOException e) {
                throw new ClassifierPersistenceException(e);
            }
        }
        return false;
    }

    @Override
    public void persistState(TrainableClassificationModel<?, ?> trainedModel) throws ClassifierPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(trainedModel);
        try (FileOutputStream output = new FileOutputStream(getFileForClassifier(persistenceSupport))) {
            persistenceSupport.saveToStream(output);
        } catch (IOException e) {
            throw new ClassifierPersistenceException(e);
        }
    }

    @Override
    public void delete(TrainableClassificationModel<?, ?> newModel) throws ClassifierPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        File classifierFile = getFileForClassifier(persistenceSupport);
        try {
            Files.deleteIfExists(classifierFile.toPath());
        } catch (IOException e) {
            throw new ClassifierPersistenceException(e);
        }
    }

    @Override
    public void deleteAll() throws ClassifierPersistenceException {
        File folder = new File(destinationFolder);
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(FILE_EXT)) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException e) {
                    throw new ClassifierPersistenceException(e);
                }
            }
        }
    }

}
