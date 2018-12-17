package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.RegressorModelFactory;
import com.sap.sailing.windestimation.model.regressor.SingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.regressor.TrainableRegressorModel;

public class SingleDimensionBasedTwdTransitionRegressorModelFactory<T extends SingleDimensionBasedTwdTransitionRegressorModelMetadata>
        implements RegressorModelFactory<TwdTransition, T> {

    private final ModelMetadataCreator<T> modelMetadataCreator;

    public SingleDimensionBasedTwdTransitionRegressorModelFactory(ModelMetadataCreator<T> modelMetadataCreator) {
        this.modelMetadataCreator = modelMetadataCreator;
    }

    @Override
    public SingleDimensionPolynomialRegressor<TwdTransition, T> getNewModel(T contextSpecificModelMetadata) {
        T modelMetadata = modelMetadataCreator.createModelMetadata();
        SingleDimensionPolynomialRegressor<TwdTransition, T> regressorModel = new SingleDimensionPolynomialRegressor<>(
                modelMetadata, 1, false);
        return regressorModel;
    }

    @Override
    public List<TrainableRegressorModel<TwdTransition, T>> getAllTrainableModels(T contextSpecificModelMetadata) {
        List<TrainableRegressorModel<TwdTransition, T>> regressors = new ArrayList<>();
        regressors.add(new SingleDimensionPolynomialRegressor<>(contextSpecificModelMetadata, 1, false));
        return regressors;
    }

    @Override
    public List<T> getAllValidContextSpecificModelMetadataCandidates(T contextSpecificModelMetadataWithMaxFeatures) {
        return Collections.singletonList(contextSpecificModelMetadataWithMaxFeatures);
    }

    public T createNewModelMetadata() {
        return modelMetadataCreator.createModelMetadata();
    }

    public SingleDimensionPolynomialRegressor<TwdTransition, T> getNewModel() {
        return getNewModel(createNewModelMetadata());
    }

    public interface ModelMetadataCreator<T> {
        T createModelMetadata();
    }

}
