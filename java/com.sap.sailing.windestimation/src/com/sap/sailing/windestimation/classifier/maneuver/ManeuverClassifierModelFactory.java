package com.sap.sailing.windestimation.classifier.maneuver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.classifier.smile.NeuralNetworkClassifier;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifierModelFactory {

    private ManeuverClassifierModelFactory() {
    }

    public static final ManeuverTypeForInternalClassification[] orderedSupportedTargetValues = {
            ManeuverTypeForInternalClassification.TACK, ManeuverTypeForInternalClassification.JIBE,
            ManeuverTypeForInternalClassification.OTHER };

    public static TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> getNewClassifierModel(
            ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        ManeuverModelMetadata modelMetadata = createModelMetadata(maneuverFeatures, boatClass);
        TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> classificationModel = new NeuralNetworkClassifier<>(
                modelMetadata);
        return classificationModel;
    }

    public static List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> getAllTrainableClassifierModels(
            ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        ManeuverModelMetadata modelMetadata = createModelMetadata(maneuverFeatures, boatClass);
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> classifiers = new ArrayList<>();
        // classifiers.add(new GradientBoostingManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new LDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new LogisticRegressionManeuverClassifier(maneuverFeatures, boatClass,
        // supportedManeuverTypes));
        // classifiers.add(new NaiveBayesManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        classifiers.add(new NeuralNetworkClassifier<>(modelMetadata));
        // classifiers.add(new QDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new RandomForestManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new SVMManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> suitableClassifiers = classifiers
                .stream().filter(classifier -> classifier.hasSupportForProvidedFeatures()).collect(Collectors.toList());
        return suitableClassifiers;
    }

    private static ManeuverModelMetadata createModelMetadata(ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        ManeuverModelMetadata modelMetadata = new ManeuverModelMetadata(maneuverFeatures, boatClass,
                orderedSupportedTargetValues);
        return modelMetadata;
    }

}
