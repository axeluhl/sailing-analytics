package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.regressor.RegressorModelFactory;
import com.sap.sailing.windestimation.model.regressor.TrainableRegressorModel;

public abstract class SingleDimensionBasedTwdTransitionRegressorModelFactory<T extends SingleDimensionBasedTwdTransitionRegressorModelMetadata>
        implements RegressorModelFactory<TwdTransition, T> {

    public List<T> getAllValidContextSpecificModelMetadataCandidates() {
        return getAllValidContextSpecificModelMetadataCandidates(null);
    }

    @Override
    public IncrementalSingleDimensionPolynomialRegressor<TwdTransition, T> getNewModel(T contextSpecificModelMetadata) {
        IncrementalSingleDimensionPolynomialRegressor<TwdTransition, T> regressorModel = new IncrementalSingleDimensionPolynomialRegressor<>(
                contextSpecificModelMetadata, contextSpecificModelMetadata.getPolynomialDegree(),
                contextSpecificModelMetadata.isWithBias());
        return regressorModel;
    }

    @Override
    public List<TrainableRegressorModel<TwdTransition, T>> getAllTrainableModels(T contextSpecificModelMetadata) {
        List<TrainableRegressorModel<TwdTransition, T>> regressors = new ArrayList<>();
        regressors.add(new IncrementalSingleDimensionPolynomialRegressor<>(contextSpecificModelMetadata,
                contextSpecificModelMetadata.getPolynomialDegree(), contextSpecificModelMetadata.isWithBias()));
        return regressors;
    }

    public abstract T createNewModelMetadata(TwdTransition twdTransition);

}
