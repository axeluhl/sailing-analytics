package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.regressor.RegressorModelFactory;
import com.sap.sailing.windestimation.model.regressor.TrainableRegressorModel;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.DegreeBearingImpl;

public abstract class SingleDimensionBasedTwdTransitionRegressorModelFactory<T extends SingleDimensionBasedTwdTransitionRegressorModelMetadata>
        implements RegressorModelFactory<TwdTransition, T> {

    @Override
    public IncrementalSingleDimensionPolynomialRegressor<TwdTransition, T> getNewModel(T contextSpecificModelMetadata) {
        IncrementalSingleDimensionPolynomialRegressor<TwdTransition, T> regressorModel = new IncrementalSingleDimensionPolynomialRegressor<>(
                contextSpecificModelMetadata,
                contextSpecificModelMetadata.getSupportedDimensionValueRange().getPolynomialDegree(),
                contextSpecificModelMetadata.getSupportedDimensionValueRange().isWithBias());
        return regressorModel;
    }

    @Override
    public List<TrainableRegressorModel<TwdTransition, T>> getAllTrainableModels(T contextSpecificModelMetadata) {
        List<TrainableRegressorModel<TwdTransition, T>> regressors = new ArrayList<>();
        regressors.add(new IncrementalSingleDimensionPolynomialRegressor<>(contextSpecificModelMetadata,
                contextSpecificModelMetadata.getSupportedDimensionValueRange().getPolynomialDegree(),
                contextSpecificModelMetadata.getSupportedDimensionValueRange().isWithBias()));
        return regressors;
    }

    public abstract T createNewModelMetadata(TwdTransition twdTransition);

    @Override
    public T getContextSpecificModelMetadataWhichModelIsAlwaysPresentAndHasMinimalFeatures() {
        TwdTransition twdTransition = new TwdTransition(new MeterDistance(100), Duration.ONE_MINUTE,
                new DegreeBearingImpl(5), ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.TACK);
        T modelMetadata = createNewModelMetadata(twdTransition);
        return modelMetadata;
    }

}
