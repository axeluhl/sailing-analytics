package com.sap.sailing.windestimation.model.store;

import java.io.OutputStream;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.ModelPersistenceException;
import com.sap.sailing.windestimation.model.TrainableModel;

public class MongoDbModelStore implements ModelStore {

    private static final String CONTEXT_NAME_PREFIX = "modelFor";
    private final MongoDatabase db;

    public MongoDbModelStore(MongoDatabase db) {
        this.db = db;
    }

    private String getFileName(PersistenceSupport persistenceSupport) {
        return persistenceSupport.getPersistenceKey();
    }

    @Override
    public <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        String fileName = getFileName(persistenceSupport);
        String bucketName = getCollectionName(newModel.getContextSpecificModelMetadata().getContextType());
        GridFSBucket gridFs = GridFSBuckets.create(db, bucketName);
        try (GridFSDownloadStream inputStream = gridFs.openDownloadStreamByName(fileName)) {
            ContextSpecificModelMetadata<?> requestedModelMetadata = newModel.getContextSpecificModelMetadata();
            @SuppressWarnings("unchecked")
            ModelType loadedModel = (ModelType) persistenceSupport.loadFromStream(inputStream);
            ContextSpecificModelMetadata<InstanceType> loadedModelMetadata = loadedModel
                    .getContextSpecificModelMetadata();
            if (!requestedModelMetadata.equals(loadedModelMetadata)) {
                throw new ModelPersistenceException("The configuration of the loaded model is: " + loadedModelMetadata
                        + ". \nExpected: " + requestedModelMetadata);
            }
            return loadedModel;
        } catch (MongoException e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public <T extends PersistableModel<?, ?>> void persistState(T trainedModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(trainedModel);
        String newFileName = getFileName(persistenceSupport);
        String bucketName = getCollectionName(trainedModel.getContextSpecificModelMetadata().getContextType());
        GridFSBucket gridFs = GridFSBuckets.create(db, bucketName);
        try {
            try (OutputStream outputStream = gridFs.openUploadStream(newFileName)) {
                persistenceSupport.saveToStream(outputStream);
            }
        } catch (Exception e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public void deleteAll(PersistenceContextType contextType) throws ModelPersistenceException {
        try {
            GridFSBuckets.create(db, getCollectionName(contextType)).drop();
        } catch (Exception e) {
            throw new ModelPersistenceException(e);
        }
    }

    private String getCollectionName(PersistenceContextType contextType) {
        return CONTEXT_NAME_PREFIX + contextType.getContextName();
    }

}
