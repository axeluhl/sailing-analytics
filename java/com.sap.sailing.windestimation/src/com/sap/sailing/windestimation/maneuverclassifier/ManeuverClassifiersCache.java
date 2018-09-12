package com.sap.sailing.windestimation.maneuverclassifier;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.ShortTimeAfterLastHitCache;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifiersCache {

    public static final int MIN_FIXES_FOR_POLARS_INFORMATION = 10000;

    private final ShortTimeAfterLastHitCache<ClassifierType, ProbabilisticManeuverClassifier> classifierCache;
    private final PolarDataService polarDataService;
    private final ManeuverFeatures maneuverFeatures;

    public ManeuverClassifiersCache(long preserveLoadedClassifiersMillis, ManeuverFeatures maneuverFeatures,
            PolarDataService polarDataService) {
        this.maneuverFeatures = maneuverFeatures;
        this.polarDataService = polarDataService;
        this.classifierCache = new ShortTimeAfterLastHitCache<ClassifierType, ProbabilisticManeuverClassifier>(
                preserveLoadedClassifiersMillis, classifierType -> ManeuverClassifierLoader
                        .loadBestClassifier(classifierType.maneuverFeatures, classifierType.boatClass));
    }

    public ProbabilisticManeuverClassifier getBestClassifier(ManeuverForEstimation maneuver) {
        ManeuverFeatures maneuverFeatures = determineFinalManeuverFeatures(maneuver);
        return classifierCache.getValue(new ClassifierType(maneuverFeatures,
                maneuverFeatures.isPolarsInformation() ? maneuver.getBoatClass() : null));
    }

    private ManeuverFeatures determineFinalManeuverFeatures(ManeuverForEstimation maneuver) {
        Long fixesCountForBoatClass = polarDataService.getFixCountPerBoatClass().get(maneuver.getBoatClass());
        boolean polars = maneuverFeatures.isPolarsInformation() && fixesCountForBoatClass != null
                && fixesCountForBoatClass >= MIN_FIXES_FOR_POLARS_INFORMATION
                && maneuver.getDeviationFromOptimalJibeAngleInDegrees() != null
                && maneuver.getDeviationFromOptimalTackAngleInDegrees() != null;
        boolean marks = maneuverFeatures.isMarksInformation() && maneuver.getRelativeBearingToNextMarkAfter() != null
                && maneuver.getRelativeBearingToNextMarkBefore() != null;
        return new ManeuverFeatures(polars, maneuverFeatures.isScaledSpeed(), marks);
    }

    public PolarDataService getPolarDataService() {
        return polarDataService;
    }

    public ManeuverFeatures getManeuverFeatures() {
        return maneuverFeatures;
    }

    private static class ClassifierType {
        private ManeuverFeatures maneuverFeatures;
        private BoatClass boatClass;

        public ClassifierType(ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
            this.maneuverFeatures = maneuverFeatures;
            this.boatClass = boatClass;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((boatClass == null) ? 0 : boatClass.getName().hashCode());
            result = prime * result + ((maneuverFeatures == null) ? 0 : maneuverFeatures.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ClassifierType other = (ClassifierType) obj;
            if (boatClass == null) {
                if (other.boatClass != null)
                    return false;
            } else if (!boatClass.getName().equals(other.boatClass.getName()))
                return false;
            if (maneuverFeatures == null) {
                if (other.maneuverFeatures != null)
                    return false;
            } else if (!maneuverFeatures.equals(other.maneuverFeatures))
                return false;
            return true;
        }
    }

}
