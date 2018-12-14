package com.sap.sailing.windestimation.model.store;

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
import com.sap.sailing.windestimation.classifier.ModelPersistenceException;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;

public class MongoDbModelStore implements ModelStore {

    private static final String CONTEXT_NAME_PREFIX = "modelFor";
    private final DB db;

    public MongoDbModelStore(DB db) {
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
        GridFS gridFs = null;
        try {
            gridFs = new GridFS(db, getCollectionName(newModel.getContextSpecificModelMetadata().getContextType()));
            List<GridFSDBFile> mongoFiles = gridFs.find(fileName);
            if (!mongoFiles.isEmpty()) {
                GridFSDBFile mongoFile = mongoFiles.get(0);
                ContextSpecificModelMetadata<?> requestedModelMetadata = newModel.getContextSpecificModelMetadata();
                try (InputStream inputStream = mongoFile.getInputStream()) {
                    @SuppressWarnings("unchecked")
                    ModelType loadedModel = (ModelType) persistenceSupport.loadFromStream(inputStream);
                    ContextSpecificModelMetadata<InstanceType> loadedModelMetadata = loadedModel
                            .getContextSpecificModelMetadata();
                    if (!requestedModelMetadata.equals(loadedModelMetadata)) {
                        throw new ModelPersistenceException("The configuration of the loaded classifier is: "
                                + loadedModelMetadata + ". \nExpected: " + requestedModelMetadata);
                    }
                    return loadedModel;
                }
            }
            return null;
        } catch (IOException e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public <T extends PersistableModel<?, ?>> void persistState(T trainedModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(trainedModel);
        String newFileName = getFileName(persistenceSupport);
        GridFS gridFs = null;
        GridFSInputFile mongoFile = null;
        try {
            gridFs = new GridFS(db, getCollectionName(trainedModel.getContextSpecificModelMetadata().getContextType()));
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
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public <T extends PersistableModel<?, ?>> void delete(T newModel) throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = checkAndGetPersistenceSupport(newModel);
        String fileName = getFileName(persistenceSupport);
        try {
            GridFS gridFs = new GridFS(db,
                    getCollectionName(newModel.getContextSpecificModelMetadata().getContextType()));
            gridFs.remove(fileName);
        } catch (Exception e) {
            throw new ModelPersistenceException(e);
        }
    }

    @Override
    public void deleteAll(ContextType contextType) throws ModelPersistenceException {
        try {
            GridFS gridFs = new GridFS(db, getCollectionName(contextType));
            DBObject dbQuery = new BasicDBObject();
            gridFs.remove(dbQuery);
        } catch (Exception e) {
            throw new ModelPersistenceException(e);
        }
    }

    private String getCollectionName(ContextType contextType) {
        return CONTEXT_NAME_PREFIX + contextType.getContextName();
    }

}
