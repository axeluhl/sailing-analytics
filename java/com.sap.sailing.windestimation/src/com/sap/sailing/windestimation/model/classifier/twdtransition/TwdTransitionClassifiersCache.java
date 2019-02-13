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

    public TwdTransitionClassifiersCache(ModelStore classifierModelStore, boolean preloadAllModels,
            long preserveLoadedClassifiersMillis) {
        super(classifierModelStore, preloadAllModels, preserveLoadedClassifiersMillis,
                new TwdTransitionClassifierModelFactory(), new TwdTransitionClassificationResultMapper());
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
