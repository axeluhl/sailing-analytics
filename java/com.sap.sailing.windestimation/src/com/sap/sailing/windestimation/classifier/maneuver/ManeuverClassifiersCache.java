package com.sap.sailing.windestimation.classifier.maneuver;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.AbstractClassifiersCache;
import com.sap.sailing.windestimation.classifier.store.ClassifierModelStore;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifiersCache extends
        AbstractClassifiersCache<ManeuverForEstimation, ManeuverModelMetadata, ManeuverWithProbabilisticTypeClassification> {

    private final ManeuverFeatures maneuverFeatures;

    public ManeuverClassifiersCache(ClassifierModelStore classifierModelStore, long preserveLoadedClassifiersMillis,
            ManeuverFeatures maxManeuverFeatures) {
        super(classifierModelStore, preserveLoadedClassifiersMillis, new ManeuverClassifierModelFactory(),
                new ManeuverClassificationResultMapper());
        this.maneuverFeatures = maxManeuverFeatures;
    }

    @Override
    public ManeuverModelMetadata getContextSpecificModelMetadata(ManeuverForEstimation maneuver) {
        ManeuverFeatures maneuverFeatures = determineFinalManeuverFeatures(maneuver);
        BoatClass boatClass = maneuverFeatures.isPolarsInformation() ? maneuver.getBoatClass() : null;
        ManeuverModelMetadata maneuverModelMetadata = new ManeuverModelMetadata(maneuverFeatures, boatClass,
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
