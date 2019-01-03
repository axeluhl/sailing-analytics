package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.TrainableRegressorModel;
import com.sap.sailing.windestimation.model.store.ModelStore;

import smile.stat.distribution.GaussianDistribution;

public class GaussianBasedTwdTransitionDistributionCache {

    private final SingleDimensionBasedTwdTransitionRegressorCache<DurationBasedTwdTransitionRegressorModelMetadata> durationBasedTwdTransitionRegressorCache;
    private final SingleDimensionBasedTwdTransitionRegressorCache<DistanceBasedTwdTransitionRegressorModelMetadata> distanceBasedTwdTransitionRegressorCache;

    public GaussianBasedTwdTransitionDistributionCache(ModelStore modelStore, long preserveLoadedModelsMillis) {
        durationBasedTwdTransitionRegressorCache = new SingleDimensionBasedTwdTransitionRegressorCache<>(modelStore,
                preserveLoadedModelsMillis, new DurationBasedTwdTransitionRegressorModelFactory());
        distanceBasedTwdTransitionRegressorCache = new SingleDimensionBasedTwdTransitionRegressorCache<>(modelStore,
                preserveLoadedModelsMillis, new DistanceBasedTwdTransitionRegressorModelFactory());
    }

    public double getCompoundDistance(TwdTransition twdTransition) {
        double durationBasedStd = durationBasedTwdTransitionRegressorCache.getBestModel(twdTransition)
                .getValue(twdTransition);
        double distanceBasedStd = distanceBasedTwdTransitionRegressorCache.getBestModel(twdTransition)
                .getValue(twdTransition);
        return durationBasedStd + distanceBasedStd;
    }

    public double getP(TwdTransition twdTransition) {
        double durationBasedP = getPFromRegressorCache(durationBasedTwdTransitionRegressorCache, twdTransition);
        double distanceBasedP = getPFromRegressorCache(distanceBasedTwdTransitionRegressorCache, twdTransition);
        return durationBasedP * distanceBasedP;
    }

    private double getPFromRegressorCache(
            SingleDimensionBasedTwdTransitionRegressorCache<? extends SingleDimensionBasedTwdTransitionRegressorModelMetadata> regressorCache,
            TwdTransition twdTransition) {
        TrainableRegressorModel<TwdTransition, ? extends SingleDimensionBasedTwdTransitionRegressorModelMetadata> regressorModel = regressorCache
                .getBestModel(twdTransition);
        double[] x = regressorModel.getContextSpecificModelMetadata().getX(twdTransition);
        double std = regressorModel.getValue(x);
        double p = getGaussianP(std, x[0]);
        return p;
    }

    private double getGaussianP(double std, double x) {
        GaussianDistribution gaussianDistribution = new GaussianDistribution(0, std);
        double p = gaussianDistribution.p(x);
        return p;
    }

}
