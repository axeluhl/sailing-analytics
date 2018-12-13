package com.sap.sailing.windestimation.classifier.twdtransition;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.AbstractClassifiersCache;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class TwdTransitionClassifiersCache
        extends AbstractClassifiersCache<TwdTransition, TwdTransitionModelMetadata, TwdTransitionClassificationResult> {

    private final boolean enableBoatClassInfo;

    public TwdTransitionClassifiersCache(ModelStore classifierModelStore,
            long preserveLoadedClassifiersMillis, boolean enableBoatClassInfo) {
        super(classifierModelStore, preserveLoadedClassifiersMillis, new TwdTransitionClassifierModelFactory(),
                new TwdTransitionClassificationResultMapper());
        this.enableBoatClassInfo = enableBoatClassInfo;
    }

    @Override
    public TwdTransitionModelMetadata getContextSpecificModelMetadata(TwdTransition twdTransition) {
        BoatClass boatClass = enableBoatClassInfo ? twdTransition.getBoatClass() : null;
        TwdTransitionModelMetadata maneuverModelMetadata = new TwdTransitionModelMetadata(boatClass);
        return maneuverModelMetadata;
    }

    public boolean isEnableBoatClassInfo() {
        return enableBoatClassInfo;
    }

}
