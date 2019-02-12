package com.sap.sailing.windestimation.model.store;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

/**
 * {@link ModelStore} which manages persistent models within MongoDB. It makes use of GridFS. Within this
 * implementation, an individual GridFS bucket is created for each managed {@link ModelDomainType} (see
 * {@link #getCollectionName(ModelDomainType)}).
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class MongoDbModelStoreImpl extends AbstractModelStoreImpl {

    private final MongoDatabase db;

    /**
     * Constructs a new instance of MongoDB model store.
     * 
     * @param db
     *            The database where {@link ModelDomainType}-dependent GridFS buckets/collections will be maintained.
     */
    public MongoDbModelStoreImpl(MongoDatabase db) {
        this.db = db;
    }

    @Override
    public <InstanceType, T extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadModel(
            ModelType newModel) throws ModelPersistenceException {
        ModelSerializationStrategy serializationStrategy = checkAndGetModelSerializationStrategy(newModel);
        String fileName = getPersistenceKey(newModel);
        String bucketName = getCollectionName(newModel.getModelContext().getDomainType());
        GridFSBucket gridFs = GridFSBuckets.create(db, bucketName);
        try (GridFSDownloadStream inputStream = gridFs.openDownloadStream(fileName)) {
            ModelContext<?> requestedModelContext = newModel.getModelContext();
            @SuppressWarnings("unchecked")
            ModelType loadedModel = (ModelType) serializationStrategy.deserializeFromStream(inputStream);
            ModelContext<InstanceType> loadedModelContext = loadedModel.getModelContext();
            verifyRequestedModelContextIsLoaded(requestedModelContext, loadedModelContext);
            return loadedModel;
        } catch (MongoException e) {
            throw new ModelNotFoundException(newModel.getModelContext(), e);
        }
    }

    @Override
    public void persistModel(PersistableModel<?, ?> trainedModel) throws ModelPersistenceException {
        ModelSerializationStrategy serializationStrategy = checkAndGetModelSerializationStrategy(trainedModel);
        String newFileName = getPersistenceKey(trainedModel);
        String bucketName = getCollectionName(trainedModel.getModelContext().getDomainType());
        GridFSBucket gridFs = GridFSBuckets.create(db, bucketName);
        try {
            try (OutputStream outputStream = gridFs.openUploadStream(newFileName)) {
                serializationStrategy.serializeToStream(trainedModel, outputStream);
            }
        } catch (Exception e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public void deleteAll(ModelDomainType domainType) throws ModelPersistenceException {
        try {
            GridFSBuckets.create(db, getCollectionName(domainType)).drop();
        } catch (Exception e) {
            throw new ModelPersistenceException(e);
        }
    }

    public static String getCollectionName(ModelDomainType domainType) {
        return CONTEXT_NAME_PREFIX + domainType.getDomainName();
    }

    @Override
    public Map<String, byte[]> exportAllPersistedModels(ModelDomainType domainType) throws ModelPersistenceException {
        Map<String, byte[]> exportedModels = new HashMap<>();
        String bucketName = getCollectionName(domainType);
        GridFSBucket gridFs = GridFSBuckets.create(db, bucketName);
        for (GridFSFile gridFSFile : gridFs.find()) {
            String fileName = gridFSFile.getFilename();
            byte[] exportedModel;
            try (GridFSDownloadStream downloadStream = gridFs.openDownloadStream(fileName)) {
                exportedModel = IOUtils.toByteArray(downloadStream);
            } catch (IOException e) {
                throw new ModelPersistenceException("Could not read model \"" + fileName + "\" from MongoDB", e);
            }
            exportedModels.put(fileName, exportedModel);
        }
        return exportedModels;
    }

    @Override
    public void importPersistedModels(Map<String, byte[]> exportedPersistedModels, ModelDomainType domainType)
            throws ModelPersistenceException {
        String bucketName = getCollectionName(domainType);
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

    @Override
    public List<PersistableModel<?, ?>> loadAllPersistedModels(ModelDomainType domainType) {
        List<PersistableModel<?, ?>> loadedModels = new ArrayList<>();
        String bucketName = getCollectionName(domainType);
        GridFSBucket gridFs = GridFSBuckets.create(db, bucketName);
        for (GridFSFile gridFSFile : gridFs.find()) {
            String fileName = gridFSFile.getFilename();
            ModelSerializationStrategy serializationStrategy = getModelSerializationStrategyFromPersistenceKey(
                    fileName);
            if (serializationStrategy == null) {
                throw new ModelLoadingException(
                        "Persistence support could not be determined due to invalid filename pattern: \"" + fileName
                                + "\"");
            }
            PersistableModel<?, ?> loadedModel;
            try (GridFSDownloadStream downloadStream = gridFs.openDownloadStream(fileName)) {
                loadedModel = serializationStrategy.deserializeFromStream(downloadStream);
            } catch (IOException e) {
                throw new ModelLoadingException("Could not read model \"" + fileName + "\" from MongoDB", e);
            }
            loadedModels.add(loadedModel);
        }
        return loadedModels;
    }

}
