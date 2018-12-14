package com.sap.sailing.windestimation.regression.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.regression.AbstractRegressionCache;

public class TwdTransitionRegressionCache
        extends AbstractRegressionCache<TwdTransition, TwdTransitionRegressionModelMetadata> {

    public TwdTransitionRegressionCache(ModelStore classifierModelStore, long preserveLoadedClassifiersMillis) {
        super(classifierModelStore, preserveLoadedClassifiersMillis, new TwdTransitionRegressionModelFactory());
    }

    @Override
    public TwdTransitionRegressionModelMetadata getContextSpecificModelMetadata(TwdTransition twdTransition) {
        TwdTransitionRegressionModelMetadata twdTransitionModelMetadata = new TwdTransitionRegressionModelMetadata();
        return twdTransitionModelMetadata;
    }

}
