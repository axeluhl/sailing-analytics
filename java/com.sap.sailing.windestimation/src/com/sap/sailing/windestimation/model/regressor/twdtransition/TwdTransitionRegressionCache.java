package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.AbstractRegressorCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class TwdTransitionRegressionCache
        extends AbstractRegressorCache<TwdTransition, TwdTransitionRegressionModelMetadata> {

    public TwdTransitionRegressionCache(ModelStore modelStore, long preserveLoadedModelsMillis) {
        super(modelStore, preserveLoadedModelsMillis, new TwdTransitionRegressionModelFactory());
    }

    @Override
    public TwdTransitionRegressionModelMetadata getContextSpecificModelMetadata(TwdTransition twdTransition) {
        TwdTransitionRegressionModelMetadata twdTransitionModelMetadata = new TwdTransitionRegressionModelMetadata();
        return twdTransitionModelMetadata;
    }

}
