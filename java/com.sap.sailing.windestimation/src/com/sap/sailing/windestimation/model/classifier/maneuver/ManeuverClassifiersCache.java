package com.sap.sailing.windestimation.model.classifier.maneuver;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.model.classifier.AbstractClassifiersCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class ManeuverClassifiersCache extends
        AbstractClassifiersCache<ManeuverForEstimation, ManeuverClassifierModelMetadata, ManeuverWithProbabilisticTypeClassification> {

    private final ManeuverFeatures maneuverFeatures;

    public ManeuverClassifiersCache(ModelStore classifierModelStore, long preserveLoadedClassifiersMillis,
            ManeuverFeatures maxManeuverFeatures) {
        super(classifierModelStore, preserveLoadedClassifiersMillis, new ManeuverClassifierModelFactory(),
                new ManeuverClassificationResultMapper());
        this.maneuverFeatures = maxManeuverFeatures;
    }

    @Override
    public ManeuverClassifierModelMetadata getContextSpecificModelMetadata(ManeuverForEstimation maneuver) {
        ManeuverFeatures maneuverFeatures = determineFinalManeuverFeatures(maneuver);
        BoatClass boatClass = maneuverFeatures.isPolarsInformation() ? maneuver.getBoatClass() : null;
        ManeuverClassifierModelMetadata maneuverModelMetadata = new ManeuverClassifierModelMetadata(maneuverFeatures, boatClass,
                ManeuverClassifierModelFactory.orderedSupportedTargetValues);
        return maneuverModelMetadata;
    }

    private ManeuverFeatures determineFinalManeuverFeatures(ManeuverForEstimation maneuver) {
        boolean polars = maneuverFeatures.isPolarsInformation()
                && maneuver.getDeviationFromOptimalJibeAngleInDegrees() != null
                && maneuver.getDeviationFromOptimalTackAngleInDegrees() != null;
        boolean marks = maneuverFeatures.isMarksInformation() && maneuver.getRelativeBearingToNextMarkAfter() != null
                && maneuver.getRelativeBearingToNextMarkBefore() != null;
        return new ManeuverFeatures(polars, maneuverFeatures.isScaledSpeed(), marks);
    }

    public ManeuverFeatures getManeuverFeatures() {
        return maneuverFeatures;
    }

}
