package com.sap.sailing.windestimation.regression.twdtransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.regression.PolynomialRegression;
import com.sap.sailing.windestimation.regression.RegressionModelFactory;
import com.sap.sailing.windestimation.regression.TrainableRegressionModel;

public class TwdTransitionRegressionModelFactory
        implements RegressionModelFactory<TwdTransition, TwdTransitionRegressionModelMetadata> {

    private static TwdTransitionRegressionModelMetadata createModelMetadata() {
        TwdTransitionRegressionModelMetadata modelMetadata = new TwdTransitionRegressionModelMetadata();
        return modelMetadata;
    }

    @Override
    public TrainableRegressionModel<TwdTransition, TwdTransitionRegressionModelMetadata> getNewClassifierModel(
            TwdTransitionRegressionModelMetadata contextSpecificModelMetadata) {
        TwdTransitionRegressionModelMetadata modelMetadata = createModelMetadata();
        TrainableRegressionModel<TwdTransition, TwdTransitionRegressionModelMetadata> regressionModel = new PolynomialRegression<>(
                modelMetadata, new int[] { 1, 1 }, new boolean[] { false, false });
        return regressionModel;
    }

    @Override
    public List<TrainableRegressionModel<TwdTransition, TwdTransitionRegressionModelMetadata>> getAllTrainableClassifierModels(
            TwdTransitionRegressionModelMetadata contextSpecificModelMetadata) {
        List<TrainableRegressionModel<TwdTransition, TwdTransitionRegressionModelMetadata>> regressors = new ArrayList<>();
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
