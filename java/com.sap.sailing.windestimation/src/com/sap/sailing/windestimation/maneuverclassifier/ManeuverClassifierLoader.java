package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.ClassifierPersistenceException;

public class ManeuverClassifierLoader {

    public static SingleManeuverClassifier loadBestClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        List<TrainableSingleManeuverOfflineClassifier> classifiers = new ArrayList<>();
        for (ManeuverFeatures possibleFeatures : ManeuverFeatures.values()) {
            if (possibleFeatures.isSubset(maneuverFeatures)) {
                TrainableSingleManeuverOfflineClassifier classifier = ManeuverClassifiersFactory
                        .getNewClassifierInstance(possibleFeatures, null);
                classifiers.add(classifier);
                if (boatClass != null) {
                    TrainableSingleManeuverOfflineClassifier classifierForBoatClass = ManeuverClassifiersFactory
                            .getNewClassifierInstance(possibleFeatures, boatClass);
                    classifiers.add(classifierForBoatClass);
                }
            }
        }

        List<TrainableSingleManeuverOfflineClassifier> loadedClassifiers = new ArrayList<>();
        for (TrainableSingleManeuverOfflineClassifier classifier : classifiers) {
            try {
                classifier.loadPersistedModel();
                if (!classifier.getManeuverFeatures().isPolarsInformation() || classifier
                        .getFixesCountForBoatClass() >= ManeuverClassifiersCache.MIN_FIXES_FOR_POLARS_INFORMATION) {
                    loadedClassifiers.add(classifier);
                }
            } catch (ClassifierPersistenceException e) {
            }
        }

        if (loadedClassifiers.isEmpty()) {
            return null;
        }
        Iterator<TrainableSingleManeuverOfflineClassifier> loadedClassifiersIterator = loadedClassifiers.iterator();
        TrainableSingleManeuverOfflineClassifier bestClassifier = loadedClassifiersIterator.next();
        while (loadedClassifiersIterator.hasNext()) {
            TrainableSingleManeuverOfflineClassifier otherClassifier = loadedClassifiersIterator.next();
            if (bestClassifier.getTestScore() < otherClassifier.getTestScore()) {
                bestClassifier = otherClassifier;
            }
        }
        return bestClassifier;
    }

}
