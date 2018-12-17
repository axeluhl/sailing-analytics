package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.AbstractRegressorCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class SingleDimensionBasedTwdTransitionRegressorCache<T extends SingleDimensionBasedTwdTransitionRegressorModelMetadata>
        extends AbstractRegressorCache<TwdTransition, T> {

    private final SingleDimensionBasedTwdTransitionRegressorModelFactory<T> modelFactory;

    public SingleDimensionBasedTwdTransitionRegressorCache(ModelStore modelStore, long preserveLoadedModelsMillis,
            SingleDimensionBasedTwdTransitionRegressorModelFactory<T> modelFactory) {
        super(modelStore, preserveLoadedModelsMillis, modelFactory);
        this.modelFactory = modelFactory;
    }

    @Override
    public T getContextSpecificModelMetadata(TwdTransition twdTransition) {
        return modelFactory.createNewModelMetadata();
    }

}
