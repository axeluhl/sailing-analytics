package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.PolynomialRegression;
import com.sap.sailing.windestimation.model.regressor.RegressionModelFactory;
import com.sap.sailing.windestimation.model.regressor.TrainableRegressorModel;

public class TwdTransitionRegressionModelFactory
        implements RegressionModelFactory<TwdTransition, TwdTransitionRegressionModelMetadata> {

    private static TwdTransitionRegressionModelMetadata createModelMetadata() {
        TwdTransitionRegressionModelMetadata modelMetadata = new TwdTransitionRegressionModelMetadata();
        return modelMetadata;
    }

    @Override
    public TrainableRegressorModel<TwdTransition, TwdTransitionRegressionModelMetadata> getNewModel(
            TwdTransitionRegressionModelMetadata contextSpecificModelMetadata) {
        TwdTransitionRegressionModelMetadata modelMetadata = createModelMetadata();
        TrainableRegressorModel<TwdTransition, TwdTransitionRegressionModelMetadata> regressionModel = new PolynomialRegression<>(
                modelMetadata, new int[] { 1, 1 }, new boolean[] { false, false });
        return regressionModel;
    }

    @Override
    public List<TrainableRegressorModel<TwdTransition, TwdTransitionRegressionModelMetadata>> getAllTrainableModels(
            TwdTransitionRegressionModelMetadata contextSpecificModelMetadata) {
        List<TrainableRegressorModel<TwdTransition, TwdTransitionRegressionModelMetadata>> regressors = new ArrayList<>();
        regressors.add(new PolynomialRegression<>(contextSpecificModelMetadata, new int[] { 1, 1 },
                new boolean[] { false, false }));
        return regressors;
    }

    @Override
    public List<TwdTransitionRegressionModelMetadata> getAllValidContextSpecificModelMetadataCandidates(
            TwdTransitionRegressionModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        return Collections.singletonList(contextSpecificModelMetadataWithMaxFeatures);
    }

}
