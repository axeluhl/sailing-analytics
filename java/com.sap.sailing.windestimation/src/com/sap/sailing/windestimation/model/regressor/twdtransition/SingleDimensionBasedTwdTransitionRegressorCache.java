package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.AbstractRegressorCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class SingleDimensionBasedTwdTransitionRegressorCache<T extends SingleDimensionBasedTwdTransitionRegressorModelContext>
        extends AbstractRegressorCache<TwdTransition, T> {

    private final SingleDimensionBasedTwdTransitionRegressorModelFactory<T> modelFactory;

    public SingleDimensionBasedTwdTransitionRegressorCache(ModelStore modelStore, boolean preloadAllModels,
            long preserveLoadedModelsMillis, SingleDimensionBasedTwdTransitionRegressorModelFactory<T> modelFactory) {
        super(modelStore, preloadAllModels, preserveLoadedModelsMillis, modelFactory);
        this.modelFactory = modelFactory;
    }

    @Override
    public T getModelContext(TwdTransition twdTransition) {
        return modelFactory.createNewModelContext(twdTransition);
    }

}
