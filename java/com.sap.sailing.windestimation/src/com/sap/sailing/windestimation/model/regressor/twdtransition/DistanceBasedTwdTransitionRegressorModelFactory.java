package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ModelFactory;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelContext.DistanceValueRange;

/**
 * {@link ModelFactory} for distance-based standard deviation of TWD delta regression models.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class DistanceBasedTwdTransitionRegressorModelFactory extends
        SingleDimensionBasedTwdTransitionRegressorModelFactory<DistanceBasedTwdTransitionRegressorModelContext> {

    @Override
    public List<DistanceBasedTwdTransitionRegressorModelContext> getAllCompatibleModelContexts(
            DistanceBasedTwdTransitionRegressorModelContext modelContextWithMaxFeatures) {
        List<DistanceBasedTwdTransitionRegressorModelContext> modelContexts = new ArrayList<>();
        modelContexts.add(modelContextWithMaxFeatures);
        return modelContexts;
    }

    @Override
    public DistanceBasedTwdTransitionRegressorModelContext createNewModelContext(TwdTransition twdTransition) {
        double metersPassed = twdTransition.getDistance().getMeters();
        for (DistanceValueRange distanceValueRange : DistanceValueRange.values()) {
            DistanceBasedTwdTransitionRegressorModelContext modelContext = new DistanceBasedTwdTransitionRegressorModelContext(
                    distanceValueRange);
            if (modelContext.isDimensionValueSupported(metersPassed)) {
                return modelContext;
            }
        }
        throw new IllegalStateException(
                "No DistanceValueRange available for distance value of " + metersPassed + " meters");
    }

}
