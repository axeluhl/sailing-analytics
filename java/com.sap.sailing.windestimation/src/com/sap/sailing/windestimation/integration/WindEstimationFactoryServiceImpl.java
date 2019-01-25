package com.sap.sailing.windestimation.integration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.windestimation.IncrementalWindEstimationTrack;
import com.sap.sailing.domain.windestimation.WindEstimationFactoryService;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.model.store.PersistenceContextType;
import com.sap.sse.mongodb.MongoDBService;

public class WindEstimationFactoryServiceImpl
        extends AbstractReplicableWithObjectInputStream<WindEstimationFactoryService, WindEstimationDataOperation<?>>
        implements ReplicableWindEstimationFactoryService {

    private static final boolean PRELOAD_ALL_MODELS = true;
    private static final boolean ENABLE_MARKS_INFORMATION = false;
    private static final boolean ENABLE_SCALED_SPEED = false;
    private static final boolean ENABLE_POLARS_INFORMATION = true;
    private static final long PRESERVE_LOADED_MODELS_MILLIS = Long.MAX_VALUE;

    public static final ModelStore MODEL_STORE = new MongoDbModelStore(MongoDBService.INSTANCE.getDB());
    private static final ManeuverFeatures MAX_MANEUVER_FEATURES = new ManeuverFeatures(ENABLE_POLARS_INFORMATION,
            ENABLE_SCALED_SPEED, ENABLE_MARKS_INFORMATION);
    private static final PersistenceContextType[] relevantContextTypes = new PersistenceContextType[] {
            PersistenceContextType.MANEUVER_CLASSIFIER, PersistenceContextType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR,
            PersistenceContextType.DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR };

    private final ManeuverClassifiersCache maneuverClassifiersCache = new ManeuverClassifiersCache(MODEL_STORE,
            PRELOAD_ALL_MODELS, PRESERVE_LOADED_MODELS_MILLIS, MAX_MANEUVER_FEATURES);
    private final GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache = new GaussianBasedTwdTransitionDistributionCache(
            MODEL_STORE, PRELOAD_ALL_MODELS, PRESERVE_LOADED_MODELS_MILLIS);
    private final List<WindEstimationModelsChangedListener> modelsChangedListeners = new ArrayList<>();
    private boolean modelsReady = false;
    private boolean shutdown = false;

    @Override
    public IncrementalWindEstimationTrack createIncrementalWindEstimationTrack(TrackedRace trackedRace) {
        IncrementalWindEstimationTrack windEstimation = new IncrementalMstHmmWindEstimationForTrackedRace(trackedRace,
                new WindSourceImpl(WindSourceType.MANEUVER_BASED_ESTIMATION), trackedRace.getPolarDataService(),
                trackedRace.getMillisecondsOverWhichToAverageWind(), maneuverClassifiersCache,
                gaussianBasedTwdTransitionDistributionCache);
        return windEstimation;
    }

    @Override
    public synchronized boolean isReady() {
        return modelsReady;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {
        for (PersistenceContextType contextType : relevantContextTypes) {
            Map<String, byte[]> exportedModels = (Map<String, byte[]>) is.readObject();
            MODEL_STORE.importPersistedModels(exportedModels, contextType);

        }
        clearState();
    }

    @Override
    public void addWindEstimationModelsChangedListenerAndReceiveUpdate(WindEstimationModelsChangedListener listener) {
        boolean modelsReady;
        synchronized (this) {
            modelsChangedListeners.add(listener);
            modelsReady = this.modelsReady;
        }
        listener.modelsChangedEvent(modelsReady);
    }

    @Override
    public synchronized void removeWindEstimationModelsChangedListener(WindEstimationModelsChangedListener listener) {
        modelsChangedListeners.remove(listener);
    }

    @Override
    public synchronized void removeAllWindEstimationModelsChangedListeners() {
        modelsChangedListeners.clear();
    }

    private void notifyModelsChangedListeners(List<WindEstimationModelsChangedListener> modelsChangedListeners,
            boolean modelsReady) {
        for (WindEstimationModelsChangedListener listener : modelsChangedListeners) {
            listener.modelsChangedEvent(modelsReady);
        }
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        for (PersistenceContextType contextType : relevantContextTypes) {
            Map<String, byte[]> exportedModels = MODEL_STORE.exportAllPersistedModels(contextType);
            objectOutputStream.writeObject(exportedModels);
        }
    }

    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        for (PersistenceContextType contextType : relevantContextTypes) {
            MODEL_STORE.deleteAll(contextType);
        }
        clearState();
    }

    @Override
    public void clearState() {
        maneuverClassifiersCache.clearCache();
        gaussianBasedTwdTransitionDistributionCache.clearCache();
        List<WindEstimationModelsChangedListener> modelsChangedListeners;
        boolean modelsReady;
        synchronized (this) {
            modelsReady = !shutdown && maneuverClassifiersCache.isReady()
                    && gaussianBasedTwdTransitionDistributionCache.isReady();
            modelsChangedListeners = this.modelsChangedListeners;
            this.modelsReady = modelsReady;
        }
        notifyModelsChangedListeners(modelsChangedListeners, modelsReady);
    }

    public void importAllModelsFromModelStore(ModelStore modelStore) throws ModelPersistenceException {
        for (PersistenceContextType contextType : relevantContextTypes) {
            Map<String, byte[]> exportedModels = modelStore.exportAllPersistedModels(contextType);
            MODEL_STORE.deleteAll(contextType);
            MODEL_STORE.importPersistedModels(exportedModels, contextType);
        }
        clearState();
    }

    public void shutdown() {
        List<WindEstimationModelsChangedListener> modelsChangedListeners;
        synchronized (this) {
            modelsReady = false;
            shutdown = true;
            modelsChangedListeners = this.modelsChangedListeners;
        }
        notifyModelsChangedListeners(modelsChangedListeners, false);
        removeAllWindEstimationModelsChangedListeners();
    }

}
