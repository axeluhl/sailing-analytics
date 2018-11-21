package com.sap.sailing.windestimation.classifier.maneuver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.TrainableManeuverClassificationModel;
import com.sap.sailing.windestimation.classifier.smile.NeuralNetworkManeuverClassifier;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifierModelFactory {

    private ManeuverClassifierModelFactory() {
    }

    public static TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> getNewClassifierModel(
            ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        return new ManeuverClassifierModel(maneuverFeatures, boatClass);
//        if (boatClass == null) {
//            return new RandomForestManeuverClassifier(maneuverFeatures, null, supportedManeuverTypes);
//        } else {
//            return new SVMManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes);
//        }
    }

    public static List<TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> getAllTrainableClassifierModels(
            ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        ManeuverModelMetadata modelMetadata = new ManeuverModelMetadata(maneuverFeatures, boatClass,
                ManeuverClassifierModel.orderedSupportedTargetValues);
        List<TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> classifiers = new ArrayList<>();
//        classifiers.add(new GradientBoostingManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new LDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new LogisticRegressionManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new NaiveBayesManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        classifiers.add(new NeuralNetworkManeuverClassifier<>(modelMetadata));
//        classifiers.add(new QDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new RandomForestManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
//        classifiers.add(new SVMManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        List<TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> suitableClassifiers = classifiers
                .stream().filter(classifier -> classifier.hasSupportForProvidedFeatures()).collect(Collectors.toList());
        return suitableClassifiers;
    }

}
