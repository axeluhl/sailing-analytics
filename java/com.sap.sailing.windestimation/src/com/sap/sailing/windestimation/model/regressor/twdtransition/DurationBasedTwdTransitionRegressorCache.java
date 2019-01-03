package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.model.store.ModelStore;

public class DurationBasedTwdTransitionRegressorCache
        extends SingleDimensionBasedTwdTransitionRegressorCache<DurationBasedTwdTransitionRegressorModelMetadata> {

    public DurationBasedTwdTransitionRegressorCache(ModelStore modelStore, long preserveLoadedModelsMillis) {
        super(modelStore, preserveLoadedModelsMillis, new DurationBasedTwdTransitionRegressorModelFactory());
    }

}
