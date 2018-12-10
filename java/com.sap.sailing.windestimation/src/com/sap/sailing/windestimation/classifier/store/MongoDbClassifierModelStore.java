package com.sap.sailing.windestimation.classifier.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.classifier.ModelMetadata;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;

public class MongoDbClassifierModelStore implements ClassifierModelStore {

    private static final String CONTEXT_NAME_PREFIX = "classifiersFor";
    private final DB db;

    public MongoDbClassifierModelStore(DB db) {
        this.db = db;
    }

    private String getFileName(PersistenceSupport<?> persistenceSupport) {
        return persistenceSupport.getPersistenceKey();
    }

    @Override
    public <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableClassificationModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ClassifierPersistenceException {
        PersistenceSupport<?> persistenceSupport = checkAndGetPersistenceSupport(newModel);
        String fileName = getFileName(persistenceSupport);
        GridFS gridFs = null;
        try {
            gridFs = new GridFS(db,
                    getCollectionName(newModel.getModelMetadata().getContextSpecificModelMetadata().getContextType()));
            List<GridFSDBFile> mongoFiles = gridFs.find(fileName);
            if (!mongoFiles.isEmpty()) {
                GridFSDBFile mongoFile = mongoFiles.get(0);
                ModelMetadata<?, ?> requestedModelMetadata = newModel.getModelMetadata();
                try (InputStream inputStream = mongoFile.getInputStream()) {
                    @SuppressWarnings("unchecked")
                    ModelType loadedModel = (ModelType) persistenceSupport.loadFromStream(inputStream);
                    ModelMetadata<InstanceType, T> loadedModelMetadata = loadedModel.getModelMetadata();
                    if (!requestedModelMetadata.equals(loadedModelMetadata)) {
                        throw new ClassifierPersistenceException("The configuration of the loaded classifier is: "
                                + loadedModelMetadata + ". \nExpected: " + requestedModelMetadata);
                    }
                    return loadedModel;
                }
            }
            return null;
        } catch (IOException e) {
            throw new ClassifierPersistenceException(e);
        }
    }

    @Override
    public void persistState(TrainableClassificationModel<?, ?> trainedModel) throws ClassifierPersistenceException {
        PersistenceSupport<?> persistenceSupport = checkAndGetPersistenceSupport(trainedModel);
        String newFileName = getFileName(persistenceSupport);
        GridFS gridFs = null;
        GridFSInputFile mongoFile = null;
        try {
            gridFs = new GridFS(db, getCollectionName(
                    trainedModel.getModelMetadata().getContextSpecificModelMetadata().getContextType()));
            mongoFile = gridFs.createFile();
            mongoFile.setFilename(newFileName);
            try (OutputStream outputStream = mongoFile.getOutputStream()) {
                persistenceSupport.saveToStream(outputStream);
            }
        } catch (Exception e) {
            if (mongoFile != null) {
                try {
                    gridFs.remove(mongoFile);
                } catch (MongoException ignore) {
                }
            }
            throw new ClassifierPersistenceException(e);
        }
    }

    @Override
    public void delete(TrainableClassificationModel<?, ?> newModel) throws ClassifierPersistenceException {
        PersistenceSupport<?> persistenceSupport = checkAndGetPersistenceSupport(newModel);
        String fileName = getFileName(persistenceSupport);
        try {
            GridFS gridFs = new GridFS(db,
                    getCollectionName(newModel.getModelMetadata().getContextSpecificModelMetadata().getContextType()));
            gridFs.remove(fileName);
        } catch (Exception e) {
            throw new ClassifierPersistenceException(e);
        }
    }

    @Override
    public void deleteAll(ContextType contextType) throws ClassifierPersistenceException {
        try {
            GridFS gridFs = new GridFS(db, getCollectionName(contextType));
            DBObject dbQuery = new BasicDBObject();
            gridFs.remove(dbQuery);
        } catch (Exception e) {
            throw new ClassifierPersistenceException(e);
        }
    }

    private String getCollectionName(ContextType contextType) {
        return CONTEXT_NAME_PREFIX + contextType.getContextName();
    }

}
