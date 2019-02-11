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
import com.sap.sailing.windestimation.model.store.InMemoryModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.ModelDomainType;

public class WindEstimationFactoryServiceImpl
        extends AbstractReplicableWithObjectInputStream<WindEstimationFactoryService, WindEstimationDataOperation<?>>
        implements ReplicableWindEstimationFactoryService {

    private static final boolean PRELOAD_ALL_MODELS = true;
    private static final boolean ENABLE_MARKS_INFORMATION = false;
    private static final boolean ENABLE_SCALED_SPEED = false;
    private static final boolean ENABLE_POLARS_INFORMATION = true;
    private static final long PRESERVE_LOADED_MODELS_MILLIS = Long.MAX_VALUE;

    private static final ManeuverFeatures MAX_MANEUVER_FEATURES = new ManeuverFeatures(ENABLE_POLARS_INFORMATION,
            ENABLE_SCALED_SPEED, ENABLE_MARKS_INFORMATION);
    private static final ModelDomainType[] relevantContextTypes = new ModelDomainType[] {
            ModelDomainType.MANEUVER_CLASSIFIER, ModelDomainType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR,
            ModelDomainType.DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR };

    public final ModelStore MODEL_STORE;
    protected final ManeuverClassifiersCache maneuverClassifiersCache;
    protected final GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache;
    private final List<WindEstimationModelsChangedListener> modelsChangedListeners = new ArrayList<>();
    private boolean modelsReady = false;
    private boolean shutdown = false;

    public WindEstimationFactoryServiceImpl() {
        MODEL_STORE = new InMemoryModelStoreImpl();
        maneuverClassifiersCache = new ManeuverClassifiersCache(MODEL_STORE, PRELOAD_ALL_MODELS,
                PRESERVE_LOADED_MODELS_MILLIS, MAX_MANEUVER_FEATURES);
        gaussianBasedTwdTransitionDistributionCache = new GaussianBasedTwdTransitionDistributionCache(MODEL_STORE,
                PRELOAD_ALL_MODELS, PRESERVE_LOADED_MODELS_MILLIS);
    }

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
        for (ModelDomainType contextType : relevantContextTypes) {
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
        for (ModelDomainType contextType : relevantContextTypes) {
            Map<String, byte[]> exportedModels = MODEL_STORE.exportAllPersistedModels(contextType);
            objectOutputStream.writeObject(exportedModels);
        }
    }

    /**
     * Deletes all persisted models in {@link #MODEL_STORE} and clears/reloads all caches with machine learning models.
     */
    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        for (ModelDomainType contextType : relevantContextTypes) {
            MODEL_STORE.deleteAll(contextType);
        }
        clearState();
    }

    /**
     * Clears/Reloads all caches with machine learning models. Notifies ready state change listeners about the new ready
     * state of this wind estimation instance.
     */
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

    /**
     * Imports all the models which are available in the provided model store.
     */
    public void importAllModelsFromModelStore(ModelStore modelStore) throws ModelPersistenceException {
        for (ModelDomainType contextType : relevantContextTypes) {
            Map<String, byte[]> exportedModels = modelStore.exportAllPersistedModels(contextType);
            MODEL_STORE.deleteAll(contextType);
            MODEL_STORE.importPersistedModels(exportedModels, contextType);
        }
        clearState();
    }

    /**
     * Shuts down the wind estimation by calling all ready state change listeners with {@code modelReadyState=false} and
     * removing all the attached listeners afterwards. This instance will never be ready again for wind estimation.
     */
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
