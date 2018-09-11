package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.impl.NeuralNetworkManeuverClassifier;
import com.sap.sailing.windestimation.maneuverclassifier.impl.RandomForestManeuverClassifier;

public class ManeuverClassifiersFactory {

    public static final ManeuverTypeForClassification[] supportedManeuverTypes = { ManeuverTypeForClassification.TACK,
            ManeuverTypeForClassification.JIBE, ManeuverTypeForClassification.OTHER };

    private ManeuverClassifiersFactory() {
    }

    public static TrainableSingleManeuverOfflineClassifier getNewClassifierInstance(ManeuverFeatures maneuverFeatures,
            BoatClass boatClass) {
        return new NeuralNetworkManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes);
//        if (boatClass == null) {
//            return new RandomForestManeuverClassifier(maneuverFeatures, null, supportedManeuverTypes);
//        } else {
//            return new SVMManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes);
//        }
    }

    public static List<TrainableSingleManeuverOfflineClassifier> getAllTrainableClassifierInstances(
            ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        List<TrainableSingleManeuverOfflineClassifier> classifiers = new ArrayList<>();
//        classifiers.add(new GradientBoostingManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new LDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new LogisticRegressionManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new NaiveBayesManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        classifiers.add(new NeuralNetworkManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new QDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        classifiers.add(new RandomForestManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new SVMManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        List<TrainableSingleManeuverOfflineClassifier> suitableClassifiers = classifiers.stream()
                .filter(classifier -> classifier.hasSupportForProvidedFeatures()).collect(Collectors.toList());
        return suitableClassifiers;
    }

}
