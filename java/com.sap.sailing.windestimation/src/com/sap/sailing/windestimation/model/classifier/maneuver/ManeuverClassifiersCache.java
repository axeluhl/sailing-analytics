package com.sap.sailing.windestimation.model.classifier.maneuver;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.model.ModelCache;
import com.sap.sailing.windestimation.model.classifier.AbstractClassifiersCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

/**
 * {@link ModelCache} which manages maneuver classifiers.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverClassifiersCache extends
        AbstractClassifiersCache<ManeuverForEstimation, ManeuverClassifierModelContext, ManeuverWithProbabilisticTypeClassification> {

    private final ManeuverFeatures maneuverFeatures;

    /**
     * Constructs a new instance of a model cache.
     * 
     * @param modelStore
     *            The model store containing all trained models which can be loaded in this cache
     * @param preloadAllModels
     *            If {@code true}, all models within the provided model store are loaded inside this cache immediately
     *            within this constructor execution. If {@code false}, the models will be loaded on-demand (lazy
     *            loading).
     * @param preserveLoadedModelsMillis
     *            If not {@link Long#MAX_VALUE}, then the in-memory cache with loaded models will drop models which
     *            where not queried for longer than the provided milliseconds. However, an evicted model will be
     *            reloaded from model store if it gets queried again.
     * @param maxManeuverFeatures
     *            The features which are allowed for use. E.g. if the provided maneuver features does not include polar
     *            features, it means that the polar feature will not be used by the models of this cache, even if the
     *            feature will be available within an input instance.
     */
    public ManeuverClassifiersCache(ModelStore modelStore, boolean preloadAllModels, long preserveLoadedModelsMillis,
            ManeuverFeatures maxManeuverFeatures) {
        super(modelStore, preloadAllModels, preserveLoadedModelsMillis, new ManeuverClassifierModelFactory(),
                new ManeuverClassificationResultMapper());
        this.maneuverFeatures = maxManeuverFeatures;
    }

    @Override
    public ManeuverClassifierModelContext getModelContext(ManeuverForEstimation maneuver) {
        ManeuverFeatures maneuverFeatures = determineFinalManeuverFeatures(maneuver);
        BoatClass boatClass = maneuverFeatures.isPolarsInformation() ? maneuver.getBoatClass() : null;
        ManeuverClassifierModelContext modelContext = new ManeuverClassifierModelContext(maneuverFeatures,
                boatClass == null ? null : boatClass.getName(),
                ManeuverClassifierModelFactory.orderedSupportedTargetValues);
        return modelContext;
    }

    /**
     * Determines the maneuver features which are available in the provided maneuver and are also enabled in
     * {@link #getManeuverFeatures()}.
     */
    private ManeuverFeatures determineFinalManeuverFeatures(ManeuverForEstimation maneuver) {
        boolean polars = maneuverFeatures.isPolarsInformation()
                && maneuver.getDeviationFromOptimalJibeAngleInDegrees() != null
                && maneuver.getDeviationFromOptimalTackAngleInDegrees() != null;
        boolean marks = maneuverFeatures.isMarksInformation() && maneuver.isMarkPassingDataAvailable();
        return new ManeuverFeatures(polars, maneuverFeatures.isScaledSpeed(), marks);
    }

    /**
     * Gets the features which are allowed for being used. E.g. if the maneuver features does not include polar
     * features, it means that the polar feature will not be used by the models of this cache, even if the feature will
     * be available within an input instance.
     */
    public ManeuverFeatures getManeuverFeatures() {
        return maneuverFeatures;
    }

}
