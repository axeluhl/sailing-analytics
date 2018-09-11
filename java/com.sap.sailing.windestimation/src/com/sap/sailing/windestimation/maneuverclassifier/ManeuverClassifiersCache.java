package com.sap.sailing.windestimation.maneuverclassifier;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.ShortTimeAfterLastHitCache;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifiersCache {

    public static final int MIN_FIXES_FOR_POLARS_INFORMATION = 10000;

    private final ShortTimeAfterLastHitCache<ClassifierType, SingleManeuverClassifier> classifierCache;
    private final boolean liveRace;
    private final PolarDataService polarDataService;
    private final boolean marksAvailable;

    public ManeuverClassifiersCache(long preserveLoadedClassifiersMillis, boolean liveRace, boolean marksAvailable,
            PolarDataService polarDataService) {
        this.liveRace = liveRace;
        this.marksAvailable = marksAvailable;
        this.polarDataService = polarDataService;
        this.classifierCache = new ShortTimeAfterLastHitCache<ClassifierType, SingleManeuverClassifier>(
                preserveLoadedClassifiersMillis, classifierType -> ManeuverClassifierLoader
                        .loadBestClassifier(classifierType.maneuverFeatures, classifierType.boatClass));
    }

    public SingleManeuverClassifier getBestClassifier(ManeuverForEstimation maneuver) {
        ManeuverFeatures maneuverFeatures = determineManeuverFeatures(maneuver);
        return classifierCache.getValue(new ClassifierType(maneuverFeatures, maneuver.getBoatClass()));
    }

    private ManeuverFeatures determineManeuverFeatures(ManeuverForEstimation maneuver) {
        Long fixesCountForBoatClass = polarDataService.getFixCountPerBoatClass().get(maneuver.getBoatClass());
        boolean polars = fixesCountForBoatClass != null && fixesCountForBoatClass >= MIN_FIXES_FOR_POLARS_INFORMATION
                && maneuver.getDeviationFromOptimalJibeAngleInDegrees() != null
                && maneuver.getDeviationFromOptimalTackAngleInDegrees() != null;
        boolean marks = marksAvailable && maneuver.getRelativeBearingToNextMarkAfter() != null
                && maneuver.getRelativeBearingToNextMarkBefore() != null;
        return new ManeuverFeatures(polars, !liveRace, marks);
    }

    public PolarDataService getPolarDataService() {
        return polarDataService;
    }

    public boolean isLiveRace() {
        return liveRace;
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
