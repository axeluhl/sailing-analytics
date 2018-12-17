package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ModelCache;
import com.sap.sailing.windestimation.model.ModelLoader;
import com.sap.sailing.windestimation.model.regressor.SingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.regressor.TrainableRegressorModel;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class TwdTransitionRegressorModelCache
        implements ModelCache<TwdTransition, DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor> {

    private DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor loadedRegressor;

    public TwdTransitionRegressorModelCache(ModelStore modelStore) {
        TwdTransitionRegressorModelFactory modelFactory = new TwdTransitionRegressorModelFactory();
        ModelLoader<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata, TrainableRegressorModel<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata>> distanceBasedRegressorLoader = new ModelLoader<>(
                modelStore, modelFactory.getDistanceBasedRegressorFactory());
        ModelLoader<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata, TrainableRegressorModel<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata>> durationBasedRegressorLoader = new ModelLoader<>(
                modelStore, modelFactory.getDurationBasedRegressorFactory());
        SingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata> loadedDistanceBasedRegressor = (SingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata>) distanceBasedRegressorLoader
                .loadBestModel(modelFactory.getDistanceBasedRegressorFactory().createNewModelMetadata());
        SingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata> loadedDurationBasedRegressor = (SingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata>) durationBasedRegressorLoader
                .loadBestModel(modelFactory.getDurationBasedRegressorFactory().createNewModelMetadata());
        this.loadedRegressor = modelFactory.getNewModel(loadedDistanceBasedRegressor, loadedDurationBasedRegressor);
    }

    @Override
    public DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor getBestModel(TwdTransition instance) {
        return loadedRegressor;
    }

}
