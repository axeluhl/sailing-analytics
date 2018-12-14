package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.PolynomialRegressor;
import com.sap.sailing.windestimation.model.regressor.RegressorModelFactory;
import com.sap.sailing.windestimation.model.regressor.TrainableRegressorModel;

public class TwdTransitionRegressorModelFactory
        implements RegressorModelFactory<TwdTransition, TwdTransitionRegressorModelMetadata> {

    private static TwdTransitionRegressorModelMetadata createModelMetadata() {
        TwdTransitionRegressorModelMetadata modelMetadata = new TwdTransitionRegressorModelMetadata();
        return modelMetadata;
    }

    @Override
    public TrainableRegressorModel<TwdTransition, TwdTransitionRegressorModelMetadata> getNewModel(
            TwdTransitionRegressorModelMetadata contextSpecificModelMetadata) {
        TwdTransitionRegressorModelMetadata modelMetadata = createModelMetadata();
        TrainableRegressorModel<TwdTransition, TwdTransitionRegressorModelMetadata> regressorModel = new PolynomialRegressor<>(
                modelMetadata, new int[] { 1, 1 }, new boolean[] { false, false });
        return regressorModel;
    }

    @Override
    public List<TrainableRegressorModel<TwdTransition, TwdTransitionRegressorModelMetadata>> getAllTrainableModels(
            TwdTransitionRegressorModelMetadata contextSpecificModelMetadata) {
        List<TrainableRegressorModel<TwdTransition, TwdTransitionRegressorModelMetadata>> regressors = new ArrayList<>();
        regressors.add(new PolynomialRegressor<>(contextSpecificModelMetadata, new int[] { 1, 1 },
                new boolean[] { false, false }));
        return regressors;
    }

    @Override
    public List<TwdTransitionRegressorModelMetadata> getAllValidContextSpecificModelMetadataCandidates(
            TwdTransitionRegressorModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        return Collections.singletonList(contextSpecificModelMetadataWithMaxFeatures);
    }

}
