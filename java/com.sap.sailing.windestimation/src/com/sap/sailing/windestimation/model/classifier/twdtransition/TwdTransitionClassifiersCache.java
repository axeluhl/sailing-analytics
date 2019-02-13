package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.AbstractClassifiersCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

/**
 * Cache with models which attempt to classify a transition whether it is correct or incorrect. This package is
 * experimental and has not been evaluated properly.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class TwdTransitionClassifiersCache extends
        AbstractClassifiersCache<TwdTransition, TwdTransitionClassifierModelContext, TwdTransitionClassificationResult> {

    /**
     * Constructs a new instance of a model cache.
     * 
     * @param modelStore
     *            The model store containing all trained models which can be loaded in this cache
     * @param preloadAllModels
     *            If {@code true}, all models within the provided model store are loaded inside this cache immediately
     *            within this constructor execution. If {@code false}, the models will be loaded on-demand (lazy
     *            loading).
     * @param preserveLoadedModelsMillis
     *            If not {@link Long#MAX_VALUE}, then the in-memory cache with loaded models will drop models which
     *            where not queried for longer than the provided milliseconds. However, an evicted model will be
     *            reloaded from model store if it gets queried again.
     */
    public TwdTransitionClassifiersCache(ModelStore modelStore, boolean preloadAllModels,
            long preserveLoadedModelsMillis) {
        super(modelStore, preloadAllModels, preserveLoadedModelsMillis, new TwdTransitionClassifierModelFactory(),
                new TwdTransitionClassificationResultMapper());
    }

    @Override
    public TwdTransitionClassifierModelContext getModelContext(TwdTransition twdTransition) {
        ManeuverTypeTransition maneuverTypeTransition = ManeuverTypeTransition
                .valueOf(twdTransition.getFromManeuverType(), twdTransition.getToManeuverType());
        TwdTransitionClassifierModelContext modelContext = new TwdTransitionClassifierModelContext(
                maneuverTypeTransition);
        return modelContext;
    }

}
