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
import com.sap.sailing.domain.windestimation.IncrementalWindEstimation;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sailing.windestimation.model.store.InMemoryModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStoreImpl;
import com.sap.sse.mongodb.MongoDBService;

public class WindEstimationFactoryServiceImpl extends
        AbstractReplicableWithObjectInputStream<ReplicableWindEstimationFactoryService, WindEstimationModelsUpdateOperation>
        implements ReplicableWindEstimationFactoryService {

    private static final boolean PRELOAD_ALL_MODELS = true;
    private static final boolean ENABLE_MARKS_INFORMATION = false;
    private static final boolean ENABLE_SCALED_SPEED = false;
    private static final boolean ENABLE_POLARS_INFORMATION = true;
    private static final long PRESERVE_LOADED_MODELS_MILLIS = Long.MAX_VALUE;

    private static final ManeuverFeatures MAX_MANEUVER_FEATURES = new ManeuverFeatures(ENABLE_POLARS_INFORMATION,
            ENABLE_SCALED_SPEED, ENABLE_MARKS_INFORMATION);
    public static final ModelDomainType[] modelDomainTypesRequiredByWindEstimation = new ModelDomainType[] {
            ModelDomainType.MANEUVER_CLASSIFIER, ModelDomainType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR,
            ModelDomainType.DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR };

    public final ModelStore MODEL_STORE;
    protected final ManeuverClassifiersCache maneuverClassifiersCache;
    protected final GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache;
    private final List<WindEstimationModelsChangedListener> modelsChangedListeners = new ArrayList<>();
    private boolean modelsReady = false;
    private boolean shutdown = false;

    public WindEstimationFactoryServiceImpl() {
        MODEL_STORE = isMaster() ? new MongoDbModelStoreImpl(MongoDBService.INSTANCE.getDB())
                : new InMemoryModelStoreImpl();
        maneuverClassifiersCache = new ManeuverClassifiersCache(MODEL_STORE, PRELOAD_ALL_MODELS,
                PRESERVE_LOADED_MODELS_MILLIS, MAX_MANEUVER_FEATURES);
        gaussianBasedTwdTransitionDistributionCache = new GaussianBasedTwdTransitionDistributionCache(MODEL_STORE,
                PRELOAD_ALL_MODELS, PRESERVE_LOADED_MODELS_MILLIS);
        modelsReady = maneuverClassifiersCache.isReady() && gaussianBasedTwdTransitionDistributionCache.isReady();
    }

    private boolean isMaster() {
        return getMasterDescriptor() == null;
    }

    @Override
    public IncrementalWindEstimation createIncrementalWindEstimationTrack(TrackedRace trackedRace) {
        IncrementalWindEstimation windEstimation = new IncrementalMstHmmWindEstimationForTrackedRace(trackedRace,
                new WindSourceImpl(WindSourceType.MANEUVER_BASED_ESTIMATION), trackedRace.getPolarDataService(),
                trackedRace.getMillisecondsOverWhichToAverageWind(), maneuverClassifiersCache,
                gaussianBasedTwdTransitionDistributionCache);
        return windEstimation;
    }

    @Override
    public synchronized boolean isReady() {
        return modelsReady;
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {
        ExportedModels exportedModels = (ExportedModels) is.readObject();
        updateWindEstimationModels(exportedModels);
    }

    @Override
    public void updateWindEstimationModels(ExportedModels exportedModels) throws ModelPersistenceException {
        for (ModelDomainType domainType : modelDomainTypesRequiredByWindEstimation) {
            Map<String, byte[]> serializedModelsForDomainType = exportedModels
                    .getSerializedModelsForDomainType(domainType);
            if (serializedModelsForDomainType != null) {
                MODEL_STORE.deleteAll(domainType);
                MODEL_STORE.importPersistedModels(serializedModelsForDomainType, domainType);
            }
        }
        reloadWindEstimationModels();
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
        ExportedModels exportedModels = new ExportedModels();
        for (ModelDomainType domainType : modelDomainTypesRequiredByWindEstimation) {
            Map<String, byte[]> exportedModelsForDomainType = MODEL_STORE.exportAllPersistedModels(domainType);
            exportedModels.addSerializedModelsForDomainType(domainType, exportedModelsForDomainType);
        }
        objectOutputStream.writeObject(exportedModels);
    }

    /**
     * Deletes all persisted models in {@link #MODEL_STORE} and clears/reloads all caches with machine learning models.
     */
    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        for (ModelDomainType domainType : modelDomainTypesRequiredByWindEstimation) {
            MODEL_STORE.deleteAll(domainType);
        }
        reloadWindEstimationModels();
    }

    @Override
    public void clearState() throws Exception {
        clearReplicaState();
    }

    /**
     * Clears/Reloads all caches with machine learning models. Notifies ready state change listeners about the new ready
     * state of this wind estimation instance.
     */
    public void reloadWindEstimationModels() {
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
        for (ModelDomainType domainType : modelDomainTypesRequiredByWindEstimation) {
            Map<String, byte[]> exportedModels = modelStore.exportAllPersistedModels(domainType);
            MODEL_STORE.deleteAll(domainType);
            MODEL_STORE.importPersistedModels(exportedModels, domainType);
        }
        reloadWindEstimationModels();
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
