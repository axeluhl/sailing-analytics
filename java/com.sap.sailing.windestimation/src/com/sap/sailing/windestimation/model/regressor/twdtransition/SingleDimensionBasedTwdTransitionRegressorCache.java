package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.AbstractRegressorCache;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.DegreeBearingImpl;

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
        return modelFactory.createNewModelMetadata(twdTransition);
    }

    @Override
    public T getContextSpecificModelMetadataWhichModelIsAlwaysPresentAndHasMinimalFeatures() {
        TwdTransition twdTransition = new TwdTransition(new MeterDistance(100), Duration.ONE_MINUTE,
                new DegreeBearingImpl(5), ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.TACK);
        T modelMetadata = getContextSpecificModelMetadata(twdTransition);
        return modelMetadata;
    }

}
