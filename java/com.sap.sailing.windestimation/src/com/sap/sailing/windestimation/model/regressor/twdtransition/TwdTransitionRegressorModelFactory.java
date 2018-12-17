package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.RegressorModelFactory;
import com.sap.sailing.windestimation.model.regressor.SingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.regressor.TrainableRegressorModel;

public class TwdTransitionRegressorModelFactory
        implements RegressorModelFactory<TwdTransition, TwdTransitionRegressorModelMetadata> {

    private final SingleDimensionBasedTwdTransitionRegressorModelFactory<DistanceBasedTwdTransitionRegressorModelMetadata> distanceBasedRegressorFactory = new SingleDimensionBasedTwdTransitionRegressorModelFactory<>(
            () -> new DistanceBasedTwdTransitionRegressorModelMetadata());
    private final SingleDimensionBasedTwdTransitionRegressorModelFactory<DurationBasedTwdTransitionRegressorModelMetadata> durationBasedRegressorFactory = new SingleDimensionBasedTwdTransitionRegressorModelFactory<>(
            () -> new DurationBasedTwdTransitionRegressorModelMetadata());

    @Override
    public DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor getNewModel(
            TwdTransitionRegressorModelMetadata contextSpecificModelMetadata) {
        SingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata> distanceBasedRegressor = distanceBasedRegressorFactory
                .getNewModel();
        SingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata> durationBasedRegressor = durationBasedRegressorFactory
                .getNewModel();
        return getNewModel(distanceBasedRegressor, durationBasedRegressor);
    }

    public DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor getNewModel(
            SingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata> distanceBasedRegressor,
            SingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata> durationBasedRegressor) {
        DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor regressorModel = new DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor(
                distanceBasedRegressor, durationBasedRegressor);
        return regressorModel;
    }

    public DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor getNewModel() {
        return getNewModel(createNewModelMetadata());
    }

    @Override
    public List<TrainableRegressorModel<TwdTransition, TwdTransitionRegressorModelMetadata>> getAllTrainableModels(
            TwdTransitionRegressorModelMetadata contextSpecificModelMetadata) {
        List<TrainableRegressorModel<TwdTransition, TwdTransitionRegressorModelMetadata>> regressors = new ArrayList<>();
        regressors.add(getNewModel(contextSpecificModelMetadata));
        return regressors;
    }

    @Override
    public List<TwdTransitionRegressorModelMetadata> getAllValidContextSpecificModelMetadataCandidates(
            TwdTransitionRegressorModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        return Collections.singletonList(contextSpecificModelMetadataWithMaxFeatures);
    }

    public TwdTransitionRegressorModelMetadata createNewModelMetadata() {
        return new TwdTransitionRegressorModelMetadata();
    }

    public SingleDimensionBasedTwdTransitionRegressorModelFactory<DistanceBasedTwdTransitionRegressorModelMetadata> getDistanceBasedRegressorFactory() {
        return distanceBasedRegressorFactory;
    }

    public SingleDimensionBasedTwdTransitionRegressorModelFactory<DurationBasedTwdTransitionRegressorModelMetadata> getDurationBasedRegressorFactory() {
        return durationBasedRegressorFactory;
    }

}
