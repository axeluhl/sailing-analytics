package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.ClassifierPersistenceException;
import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.RandomForestManeuverClassifier;

public class ManeuverClassifierLoader {

    public static final ManeuverTypeForClassification[] supportedManeuverTypes = { ManeuverTypeForClassification.TACK,
            ManeuverTypeForClassification.JIBE, ManeuverTypeForClassification.OTHER };

    public static SingleManeuverClassifier loadBestClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        List<RandomForestManeuverClassifier> classifiers = new ArrayList<>();
        for (ManeuverFeatures possibleFeatures : ManeuverFeatures.values()) {
            if (possibleFeatures.isSubset(maneuverFeatures)) {
                RandomForestManeuverClassifier classifier = new RandomForestManeuverClassifier(possibleFeatures, null,
                        supportedManeuverTypes);
                classifiers.add(classifier);
                if (boatClass != null) {
                    RandomForestManeuverClassifier classifierForBoatClass = new RandomForestManeuverClassifier(
                            possibleFeatures, boatClass, supportedManeuverTypes);
                    classifiers.add(classifierForBoatClass);
                }
            }
        }

        List<RandomForestManeuverClassifier> loadedClassifiers = new ArrayList<>();
        for (RandomForestManeuverClassifier classifier : classifiers) {
            try {
                classifier.loadPersistedModel();
                loadedClassifiers.add(classifier);
            } catch (ClassifierPersistenceException e) {
            }
        }

        if (loadedClassifiers.isEmpty()) {
            return null;
        }
        Iterator<RandomForestManeuverClassifier> loadedClassifiersIterator = loadedClassifiers.iterator();
        RandomForestManeuverClassifier bestClassifier = loadedClassifiersIterator.next();
        while (loadedClassifiersIterator.hasNext()) {
            RandomForestManeuverClassifier otherClassifier = loadedClassifiersIterator.next();
            if (bestClassifier.getTestScore() < otherClassifier.getTestScore()) {
                bestClassifier = otherClassifier;
            }
        }
        return bestClassifier;
    }

}
