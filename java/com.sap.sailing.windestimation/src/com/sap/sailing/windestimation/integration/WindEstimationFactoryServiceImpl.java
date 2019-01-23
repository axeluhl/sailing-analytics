package com.sap.sailing.windestimation.integration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Map;

import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.windestimation.IncrementalWindEstimationTrack;
import com.sap.sailing.domain.windestimation.WindEstimationFactoryService;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.model.store.PersistenceContextType;
import com.sap.sse.mongodb.MongoDBService;

public class WindEstimationFactoryServiceImpl
        extends AbstractReplicableWithObjectInputStream<WindEstimationFactoryService, WindEstimationDataOperation<?>>
        implements ReplicableWindEstimationFactoryService {

    private static final boolean ENABLE_MARKS_INFORMATION = false;
    private static final boolean ENABLE_SCALED_SPEED = false;
    private static final boolean ENABLE_POLARS_INFORMATION = true;
    private static final long PRESERVE_LOADED_MODELS_MILLIS = Long.MAX_VALUE;

    private static final ModelStore MODEL_STORE = new MongoDbModelStore(MongoDBService.INSTANCE.getDB());
    private static final ManeuverFeatures MAX_MANEUVER_FEATURES = new ManeuverFeatures(ENABLE_POLARS_INFORMATION,
            ENABLE_SCALED_SPEED, ENABLE_MARKS_INFORMATION);

    private final ManeuverClassifiersCache maneuverClassifiersCache = new ManeuverClassifiersCache(MODEL_STORE,
            PRESERVE_LOADED_MODELS_MILLIS, MAX_MANEUVER_FEATURES);
    private final GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache = new GaussianBasedTwdTransitionDistributionCache(
            MODEL_STORE, PRESERVE_LOADED_MODELS_MILLIS);

    @Override
    public IncrementalWindEstimationTrack createIncrementalWindEstimationTrack(TrackedRace trackedRace) {
        IncrementalWindEstimationTrack windEstimation = null;
        if (maneuverClassifiersCache.isReady() && gaussianBasedTwdTransitionDistributionCache.isReady()) {
            windEstimation = new IncrementalMstHmmWindEstimationForTrackedRace(trackedRace,
                    new WindSourceImpl(WindSourceType.MANEUVER_BASED_ESTIMATION), trackedRace.getPolarDataService(),
                    trackedRace.getMillisecondsOverWhichToAverageWind(), maneuverClassifiersCache,
                    gaussianBasedTwdTransitionDistributionCache);
        }
        return windEstimation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {
        Map<String, byte[]> exportedModels = (Map<String, byte[]>) is.readObject();
        MODEL_STORE.importPersistedModels(exportedModels, PersistenceContextType.MANEUVER_CLASSIFIER);
        exportedModels = (Map<String, byte[]>) is.readObject();
        MODEL_STORE.importPersistedModels(exportedModels,
                PersistenceContextType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR);
        exportedModels = (Map<String, byte[]>) is.readObject();
        MODEL_STORE.importPersistedModels(exportedModels,
                PersistenceContextType.DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR);
        clearState();
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        Map<String, byte[]> exportedModels = MODEL_STORE
                .exportAllPersistedModels(PersistenceContextType.MANEUVER_CLASSIFIER);
        objectOutputStream.writeObject(exportedModels);
        exportedModels = MODEL_STORE
                .exportAllPersistedModels(PersistenceContextType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR);
        objectOutputStream.writeObject(exportedModels);
        exportedModels = MODEL_STORE
                .exportAllPersistedModels(PersistenceContextType.DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR);
        objectOutputStream.writeObject(exportedModels);
    }

    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        MODEL_STORE.deleteAll(PersistenceContextType.MANEUVER_CLASSIFIER);
        MODEL_STORE.deleteAll(PersistenceContextType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR);
        MODEL_STORE.deleteAll(PersistenceContextType.DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR);
        clearState();
    }

    @Override
    public void clearState() {
        maneuverClassifiersCache.clearCache();
        gaussianBasedTwdTransitionDistributionCache.clearCache();
    }

}
