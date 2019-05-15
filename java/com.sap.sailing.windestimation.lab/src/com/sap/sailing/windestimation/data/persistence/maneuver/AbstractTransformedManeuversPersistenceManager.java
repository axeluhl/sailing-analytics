package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.sap.sailing.windestimation.util.LoggingUtil;

public abstract class AbstractTransformedManeuversPersistenceManager<T> extends AbstractPersistenceManager<T>
        implements TransformedManeuversPersistenceManager<T> {

    private final PersistenceManager<?>[] dependencyOnOtherPersistenceManagers;

    public AbstractTransformedManeuversPersistenceManager(PersistenceManager<?>... dependencyToOtherPersistenceManagers)
            throws UnknownHostException {
        this.dependencyOnOtherPersistenceManagers = dependencyToOtherPersistenceManagers;
    }

    protected abstract List<? extends Bson> getMongoDbAggregationPipelineForTransformation() throws UnknownHostException;

    public void createIfNotExistsCollectionWithTransformedManeuvers() throws UnknownHostException {
        boolean collectionExists = collectionExists();
        if (!collectionExists || countElements() < 1) {
            if(collectionExists) {
                dropCollection();
            }
            for (PersistenceManager<?> persistenceManager : dependencyOnOtherPersistenceManagers) {
                if (!persistenceManager.collectionExists()) {
                    if (persistenceManager instanceof TransformedManeuversPersistenceManager) {
                        ((TransformedManeuversPersistenceManager<?>) persistenceManager)
                                .createCollectionWithTransformedManeuvers();
                    } else {
                        throw new RuntimeException("Collection \"" + persistenceManager.getCollectionName()
                                + "\" required for transformation does not exist");
                    }
                }
            }
            createCollectionWithTransformedManeuvers();
        }
    }
    
    /**
     * The collection to be used in the {@code $out} stage of the aggregation pipeline produced by
     * {@link #getMongoDbAggregationPipelineForTransformation()}.
     */
    abstract protected MongoCollection<?> getCollectionForTransformation() throws UnknownHostException;

    /**
     * Produces a {@code $out} aggregation pipeline step that specifies this manager's {@link #getCollectionName()
     * collection name}.
     */
    protected Document getOutPipelineStage() {
        return Document.parse("{$out: '" + getCollectionName() + "'}");
    }

    public void createCollectionWithTransformedManeuvers() throws UnknownHostException {
        for (PersistenceManager<?> persistenceManager : dependencyOnOtherPersistenceManagers) {
            if (!persistenceManager.collectionExists()) {
                throw new RuntimeException("Collection \"" + persistenceManager.getCollectionName()
                        + "\" required for transformation does not exist");
            }
        }
        dropCollection();
        LoggingUtil.logInfo("Transformation for \"" + getCollectionName() + "\" collection started.");
        getDb().getCollection("Humba").aggregate(new ArrayList<Bson>());
        Object result = getCollectionForTransformation().aggregate(getMongoDbAggregationPipelineForTransformation());
        long numberOfElements = countElements();
        if (numberOfElements < 1) {
            dropCollection();
            throw new RuntimeException("Transformation failed. The collection with transformed was empty.");
        }
        LoggingUtil.logInfo("Transformation succeeded. Collection \"" + getCollectionName() + "\" created with "
                + numberOfElements + " elements.\n" + result);
    }

}
