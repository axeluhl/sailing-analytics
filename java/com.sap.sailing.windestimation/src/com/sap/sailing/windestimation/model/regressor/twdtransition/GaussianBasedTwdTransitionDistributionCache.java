package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.store.ModelStore;

import smile.stat.distribution.GaussianDistribution;

/**
 * A custom model cache which manages internally two instances of
 * {@link SingleDimensionBasedTwdTransitionRegressorCache}, one for the distance dimension, and one for the duration
 * dimension. The class offers few convenience methods which calculate various metrics using both dimensions. Moreover,
 * it introduces strategies for joining probabilities of both dimensions.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GaussianBasedTwdTransitionDistributionCache {

    private final SingleDimensionBasedTwdTransitionRegressorCache<DurationBasedTwdTransitionRegressorModelContext> durationBasedTwdTransitionRegressorCache;
    private final SingleDimensionBasedTwdTransitionRegressorCache<DistanceBasedTwdTransitionRegressorModelContext> distanceBasedTwdTransitionRegressorCache;
    private final boolean preloadAllModels;

    /**
     * Creates internally two model caches, one for distance dimension, one for duration dimension.
     * 
     * @param modelStore
     *            The model store containing all trained distance- and duration-based models which can be loaded in this
     *            cache
     * @param preloadAllModels
     *            If {@code true}, all distance- and duration-based models within the provided model store are loaded
     *            inside this cache immediately within this constructor execution. If {@code false}, the models will be
     *            loaded on-demand (lazy loading).
     * @param preserveLoadedModelsMillis
     *            If not {@link Long#MAX_VALUE}, then the in-memory cache with loaded models will drop models which
     *            where not queried for longer than the provided milliseconds. However, an evicted model will be
     *            reloaded from model store if it gets queried again.
     */
    public GaussianBasedTwdTransitionDistributionCache(ModelStore modelStore, boolean preloadAllModels,
            long preserveLoadedModelsMillis) {
        this.preloadAllModels = preloadAllModels;
        durationBasedTwdTransitionRegressorCache = new SingleDimensionBasedTwdTransitionRegressorCache<>(modelStore,
                preloadAllModels, preserveLoadedModelsMillis, new DurationBasedTwdTransitionRegressorModelFactory());
        distanceBasedTwdTransitionRegressorCache = new SingleDimensionBasedTwdTransitionRegressorCache<>(modelStore,
                preloadAllModels, preserveLoadedModelsMillis, new DistanceBasedTwdTransitionRegressorModelFactory());
    }

    /**
     * Gets a distance measure which considers distance and duration within the provided TWD transition. More in detail,
     * two standard deviations for TWD delta are calculated, one by considering the distance dimension, another by
     * considering the duration dimension. The compound distance will be the sum of both standard deviation values.
     */
    public double getCompoundDistance(TwdTransition twdTransition) {
        double durationBasedStd = durationBasedTwdTransitionRegressorCache.getBestModel(twdTransition)
                .getValue(twdTransition);
        double distanceBasedStd = distanceBasedTwdTransitionRegressorCache.getBestModel(twdTransition)
                .getValue(twdTransition);
        return durationBasedStd + distanceBasedStd;
    }

    /**
     * Gets the probability for being the provided TWD transition valid/true. The probability is determined considering
     * the seconds, meters and TWD delta of the provided TWD transition. More in detail, two standard deviations for TWD
     * delta are calculated, one by considering the distance dimension, another by considering the duration dimension. A
     * final standard deviation is calculated as sum of both standard deviation values. Then a Gaussian Distribution is
     * initialized with zero mean the the final standard deviation. The transition probability is sampled from the
     * Gaussian Distribution at position of TWD delta in degrees.
     */
    public double getProbability(TwdTransition twdTransition) {
        double stdSum = getCompoundDistance(twdTransition);
        double p = getGaussianProbability(stdSum, twdTransition.getTwdChange().abs().getDegrees());
        return p;
    }

    /**
     * Samples the probability at provided position x within a Gaussian Distribution which is parametrized with the
     * provided standard deviation std and zero mean.
     */
    public double getGaussianProbability(double std, double x) {
        GaussianDistribution gaussianDistribution = new GaussianDistribution(0, std);
        double p = gaussianDistribution.p(x);
        return p;
    }

    /**
     * Clears the caches with already loaded distance- and duration-based models. If {@link #isPreloadAllModels()} is
     * {@code true}, then the caches will be immediately refilled from the model store provided to this cache instance.
     */
    public void clearCache() {
        durationBasedTwdTransitionRegressorCache.clearCache();
        distanceBasedTwdTransitionRegressorCache.clearCache();
    }

    /**
     * Checks whether the cache is ready for usage. It is ready if its distance- and duration-based models are
     * successfully trained and can both return a non {@code null} by
     * {@link SingleDimensionBasedTwdTransitionRegressorCache#getBestModel(Object)} for all possible input instances.
     */
    public boolean isReady() {
        return durationBasedTwdTransitionRegressorCache.isReady() && distanceBasedTwdTransitionRegressorCache.isReady();
    }

    /**
     * If {@code true}, then this cache is operating in eager fetching mode meaning that all models are initially
     * preloaded in the in-memory cache. If {@code false}, then this model is operating in the lazy-mode, meaning that
     * the in-memory cache will be filled on-demand only. This configuration flag has also impact on
     * {@link #clearCache()}.
     */
    public boolean isPreloadAllModels() {
        return preloadAllModels;
    }

}
