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

public abstract class SingleDimensionBasedTwdTransitionRegressorModelFactory<MC extends SingleDimensionBasedTwdTransitionRegressorModelContext>
        implements RegressorModelFactory<TwdTransition, MC> {

    @Override
    public IncrementalSingleDimensionPolynomialRegressor<TwdTransition, MC> getNewModel(MC modelContext) {
        IncrementalSingleDimensionPolynomialRegressor<TwdTransition, MC> regressorModel = new IncrementalSingleDimensionPolynomialRegressor<>(
                modelContext,
                modelContext.getSupportedDimensionValueRange().getPolynomialDegree(),
                modelContext.getSupportedDimensionValueRange().isWithBias());
        return regressorModel;
    }

    @Override
    public List<TrainableRegressorModel<TwdTransition, MC>> getAllTrainableModels(MC modelContext) {
        List<TrainableRegressorModel<TwdTransition, MC>> regressors = new ArrayList<>();
        regressors.add(new IncrementalSingleDimensionPolynomialRegressor<>(modelContext,
                modelContext.getSupportedDimensionValueRange().getPolynomialDegree(),
                modelContext.getSupportedDimensionValueRange().isWithBias()));
        return regressors;
    }

    public abstract MC createNewModelContext(TwdTransition twdTransition);

    @Override
    public MC getContextSpecificModelContextWhichModelIsAlwaysPresentAndHasMinimalFeatures() {
        TwdTransition twdTransition = new TwdTransition(new MeterDistance(100), Duration.ONE_MINUTE,
                new DegreeBearingImpl(5), ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.TACK);
        MC modelContext = createNewModelContext(twdTransition);
        return modelContext;
    }

}
