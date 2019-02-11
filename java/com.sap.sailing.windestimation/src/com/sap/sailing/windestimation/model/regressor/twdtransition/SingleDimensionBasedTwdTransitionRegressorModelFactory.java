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

public abstract class SingleDimensionBasedTwdTransitionRegressorModelFactory<T extends SingleDimensionBasedTwdTransitionRegressorModelContext>
        implements RegressorModelFactory<TwdTransition, T> {

    @Override
    public IncrementalSingleDimensionPolynomialRegressor<TwdTransition, T> getNewModel(T modelContext) {
        IncrementalSingleDimensionPolynomialRegressor<TwdTransition, T> regressorModel = new IncrementalSingleDimensionPolynomialRegressor<>(
                modelContext,
                modelContext.getSupportedDimensionValueRange().getPolynomialDegree(),
                modelContext.getSupportedDimensionValueRange().isWithBias());
        return regressorModel;
    }

    @Override
    public List<TrainableRegressorModel<TwdTransition, T>> getAllTrainableModels(T modelContext) {
        List<TrainableRegressorModel<TwdTransition, T>> regressors = new ArrayList<>();
        regressors.add(new IncrementalSingleDimensionPolynomialRegressor<>(modelContext,
                modelContext.getSupportedDimensionValueRange().getPolynomialDegree(),
                modelContext.getSupportedDimensionValueRange().isWithBias()));
        return regressors;
    }

    public abstract T createNewModelContext(TwdTransition twdTransition);

    @Override
    public T getContextSpecificModelContextWhichModelIsAlwaysPresentAndHasMinimalFeatures() {
        TwdTransition twdTransition = new TwdTransition(new MeterDistance(100), Duration.ONE_MINUTE,
                new DegreeBearingImpl(5), ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.TACK);
        T modelContext = createNewModelContext(twdTransition);
        return modelContext;
    }

}
