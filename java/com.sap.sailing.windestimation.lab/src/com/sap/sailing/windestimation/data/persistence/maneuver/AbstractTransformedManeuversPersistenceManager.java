package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.util.LoggingUtil;

public abstract class AbstractTransformedManeuversPersistenceManager<T>
        extends AbstractPersistenceManager<T> implements TransformedManeuversPersistenceManager<T> {

    private final PersistenceManager<?>[] dependencyToOtherPersistenceManagers;

    public AbstractTransformedManeuversPersistenceManager(PersistenceManager<?>... dependencyToOtherPersistenceManagers)
            throws UnknownHostException {
        this.dependencyToOtherPersistenceManagers = dependencyToOtherPersistenceManagers;
    }

    protected abstract String getMongoDbEvalStringForTransformation();

    public void createIfNotExistsCollectionWithTransformedManeuvers() {
        if (!collectionExists()) {
            for (PersistenceManager<?> persistenceManager : dependencyToOtherPersistenceManagers) {
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
        for (PersistenceManager<?> persistenceManager : dependencyToOtherPersistenceManagers) {
            if (!persistenceManager.collectionExists()) {
                throw new RuntimeException("Collection \"" + persistenceManager.getCollectionName()
                        + "\" required for transformation does not exist");
            }
        }
        dropCollection();
        LoggingUtil.logInfo("Transformation for \"" + getCollectionName() + "\" collection started.");
        Object result = getDb().eval(getMongoDbEvalStringForTransformation());
        LoggingUtil.logInfo("Transformation succeeded. Collection \"" + getCollectionName() + "\" created.\n" + result);
    }

}
