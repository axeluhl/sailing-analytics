package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.store.ModelStore;

import smile.stat.distribution.GaussianDistribution;

public class GaussianBasedTwdTransitionDistributionCache {

    private final SingleDimensionBasedTwdTransitionRegressorCache<DurationBasedTwdTransitionRegressorModelMetadata> durationBasedTwdTransitionRegressorCache;
    private final SingleDimensionBasedTwdTransitionRegressorCache<DistanceBasedTwdTransitionRegressorModelMetadata> distanceBasedTwdTransitionRegressorCache;

    public GaussianBasedTwdTransitionDistributionCache(ModelStore modelStore, boolean preloadAllModels,
            long preserveLoadedModelsMillis) {
        durationBasedTwdTransitionRegressorCache = new SingleDimensionBasedTwdTransitionRegressorCache<>(modelStore,
                preloadAllModels, preserveLoadedModelsMillis, new DurationBasedTwdTransitionRegressorModelFactory());
        distanceBasedTwdTransitionRegressorCache = new SingleDimensionBasedTwdTransitionRegressorCache<>(modelStore,
                preloadAllModels, preserveLoadedModelsMillis, new DistanceBasedTwdTransitionRegressorModelFactory());
    }

    public double getCompoundDistance(TwdTransition twdTransition) {
        double durationBasedStd = durationBasedTwdTransitionRegressorCache.getBestModel(twdTransition)
                .getValue(twdTransition);
        double distanceBasedStd = distanceBasedTwdTransitionRegressorCache.getBestModel(twdTransition)
                .getValue(twdTransition);
        return durationBasedStd + distanceBasedStd;
    }

    public double getP(TwdTransition twdTransition) {
        double stdSum = getCompoundDistance(twdTransition);
        double p = getGaussianP(stdSum, twdTransition.getTwdChange().abs().getDegrees());
        return p;
    }

    private double getGaussianP(double std, double x) {
        GaussianDistribution gaussianDistribution = new GaussianDistribution(0, std);
        double p = gaussianDistribution.p(x);
        return p;
    }

    public void clearCache() {
        durationBasedTwdTransitionRegressorCache.clearCache();
        distanceBasedTwdTransitionRegressorCache.clearCache();
    }

    public boolean isReady() {
        return durationBasedTwdTransitionRegressorCache.isReady() && distanceBasedTwdTransitionRegressorCache.isReady();
    }

}
