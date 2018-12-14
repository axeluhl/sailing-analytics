package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.AbstractRegressorCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class TwdTransitionRegressorCache
        extends AbstractRegressorCache<TwdTransition, TwdTransitionRegressorModelMetadata> {

    public TwdTransitionRegressorCache(ModelStore modelStore, long preserveLoadedModelsMillis) {
        super(modelStore, preserveLoadedModelsMillis, new TwdTransitionRegressorModelFactory());
    }

    @Override
    public TwdTransitionRegressorModelMetadata getContextSpecificModelMetadata(TwdTransition twdTransition) {
        TwdTransitionRegressorModelMetadata twdTransitionModelMetadata = new TwdTransitionRegressorModelMetadata();
        return twdTransitionModelMetadata;
    }

}
