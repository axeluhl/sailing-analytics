package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.AbstractClassifiersCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class TwdTransitionClassifiersCache extends
        AbstractClassifiersCache<TwdTransition, TwdTransitionClassifierModelMetadata, TwdTransitionClassificationResult> {

    public TwdTransitionClassifiersCache(ModelStore classifierModelStore, boolean preloadAllModels,
            long preserveLoadedClassifiersMillis) {
        super(classifierModelStore, preloadAllModels, preserveLoadedClassifiersMillis,
                new TwdTransitionClassifierModelFactory(), new TwdTransitionClassificationResultMapper());
    }

    @Override
    public TwdTransitionClassifierModelMetadata getContextSpecificModelMetadata(TwdTransition twdTransition) {
        ManeuverTypeTransition maneuverTypeTransition = ManeuverTypeTransition
                .valueOf(twdTransition.getFromManeuverType(), twdTransition.getToManeuverType());
        TwdTransitionClassifierModelMetadata twdTrasitionModelMetadata = new TwdTransitionClassifierModelMetadata(
                maneuverTypeTransition);
        return twdTrasitionModelMetadata;
    }

}
