package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import org.bson.Document;

import com.sap.sailing.windestimation.util.LoggingUtil;

public abstract class AbstractTransformedManeuversPersistenceManager<T> extends AbstractPersistenceManager<T>
        implements TransformedManeuversPersistenceManager<T> {

    private final PersistenceManager<?>[] dependencyOnOtherPersistenceManagers;

    public AbstractTransformedManeuversPersistenceManager(PersistenceManager<?>... dependencyToOtherPersistenceManagers)
            throws UnknownHostException {
        this.dependencyOnOtherPersistenceManagers = dependencyToOtherPersistenceManagers;
    }

    protected abstract String getMongoDbEvalStringForTransformation();

    public void createIfNotExistsCollectionWithTransformedManeuvers() {
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

    public void createCollectionWithTransformedManeuvers() {
        for (PersistenceManager<?> persistenceManager : dependencyOnOtherPersistenceManagers) {
            if (!persistenceManager.collectionExists()) {
                throw new RuntimeException("Collection \"" + persistenceManager.getCollectionName()
                        + "\" required for transformation does not exist");
            }
        }
        dropCollection();
        LoggingUtil.logInfo("Transformation for \"" + getCollectionName() + "\" collection started.");
        Object result = getDb().runCommand(Document.parse(getMongoDbEvalStringForTransformation()));
        long numberOfElements = countElements();
        if (numberOfElements < 1) {
            dropCollection();
            throw new RuntimeException("Transformation failed. The collection with transformed was empty.");
        }
        LoggingUtil.logInfo("Transformation succeeded. Collection \"" + getCollectionName() + "\" created with "
                + numberOfElements + " elements.\n" + result);
    }

}
