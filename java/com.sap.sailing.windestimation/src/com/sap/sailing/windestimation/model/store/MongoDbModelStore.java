package com.sap.sailing.windestimation.model.store;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

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
            throw new ModelNotFoundException(newModel.getContextSpecificModelMetadata(), e);
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

    @Override
    public Map<String, byte[]> exportAllPersistedModels(PersistenceContextType contextType)
            throws ModelPersistenceException {
        Map<String, byte[]> exportedModels = new HashMap<>();
        String bucketName = getCollectionName(contextType);
        GridFSBucket gridFs = GridFSBuckets.create(db, bucketName);
        for (GridFSFile gridFSFile : gridFs.find()) {
            String fileName = gridFSFile.getFilename();
            byte[] exportedModel;
            try (GridFSDownloadStream downloadStream = gridFs.openDownloadStreamByName(fileName)) {
                exportedModel = IOUtils.toByteArray(downloadStream);
            } catch (IOException e) {
                throw new ModelPersistenceException("Could not read model \"" + fileName + "\" from MongoDB", e);
            }
            exportedModels.put(fileName, exportedModel);
        }
        return exportedModels;
    }

    @Override
    public void importPersistedModels(Map<String, byte[]> exportedPersistedModels, PersistenceContextType contextType)
            throws ModelPersistenceException {
        String bucketName = getCollectionName(contextType);
        GridFSBucket gridFs = GridFSBuckets.create(db, bucketName);
        for (Entry<String, byte[]> entry : exportedPersistedModels.entrySet()) {
            String fileName = entry.getKey();
            byte[] exportedModel = entry.getValue();
            try {
                try (OutputStream outputStream = gridFs.openUploadStream(fileName)) {
                    outputStream.write(exportedModel);
                }
            } catch (Exception e) {
                throw new ModelPersistenceException("Could not store model \"" + fileName + "\" in MongoDB", e);
            }
        }
    }

}
