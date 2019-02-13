package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ModelFactory;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionRegressorModelContext.DurationValueRange;

/**
 * {@link ModelFactory} for duration-based standard deviation of TWD delta regression models.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class DurationBasedTwdTransitionRegressorModelFactory extends
        SingleDimensionBasedTwdTransitionRegressorModelFactory<DurationBasedTwdTransitionRegressorModelContext> {

    @Override
    public List<DurationBasedTwdTransitionRegressorModelContext> getAllCompatibleModelContexts(
            DurationBasedTwdTransitionRegressorModelContext modelContextWithMaxFeatures) {
        List<DurationBasedTwdTransitionRegressorModelContext> modelContexts = new ArrayList<>();
        modelContexts.add(modelContextWithMaxFeatures);
        return modelContexts;
    }

    @Override
    public DurationBasedTwdTransitionRegressorModelContext createNewModelContext(TwdTransition twdTransition) {
        double secondsPassed = twdTransition.getDuration().asSeconds();
        for (DurationValueRange durationValueRange : DurationValueRange.values()) {
            DurationBasedTwdTransitionRegressorModelContext modelContext = new DurationBasedTwdTransitionRegressorModelContext(
                    durationValueRange);
            if (modelContext.isDimensionValueSupported(secondsPassed)) {
                return modelContext;
            }
        }
        throw new IllegalStateException(
                "No DurationValueRange available for duration value of " + secondsPassed + " seconds");
    }

}
