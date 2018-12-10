package com.sap.sailing.windestimation.classifier.maneuver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.ClassifierModelFactory;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.classifier.smile.NeuralNetworkClassifier;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifierModelFactory
        implements ClassifierModelFactory<ManeuverForEstimation, ManeuverModelMetadata> {

    public static final ManeuverTypeForInternalClassification[] orderedSupportedTargetValues = {
            ManeuverTypeForInternalClassification.TACK, ManeuverTypeForInternalClassification.JIBE,
            ManeuverTypeForInternalClassification.OTHER };

    @Override
    public TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> getNewClassifierModel(
            ManeuverModelMetadata contextSpecificModelMetadata) {
        TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> classificationModel = new NeuralNetworkClassifier<>(
                contextSpecificModelMetadata);
        return classificationModel;
    }

    @Override
    public List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> getAllTrainableClassifierModels(
            ManeuverModelMetadata contextSpecificModelMetadata) {
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> classifiers = new ArrayList<>();
        // classifiers.add(new GradientBoostingManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new LDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new LogisticRegressionManeuverClassifier(maneuverFeatures, boatClass,
        // supportedManeuverTypes));
        // classifiers.add(new NaiveBayesManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        classifiers.add(new NeuralNetworkClassifier<>(contextSpecificModelMetadata));
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

    @Override
    public List<ManeuverModelMetadata> getAllValidContextSpecificModelMetadataCandidates(
            ManeuverModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        ManeuverFeatures maneuverFeatures = contextSpecificModelMetadataWithMaxFeatures.getManeuverFeatures();
        BoatClass boatClass = contextSpecificModelMetadataWithMaxFeatures.getBoatClass();
        List<ManeuverModelMetadata> modelMetadataCandidates = new ArrayList<>();
        for (ManeuverFeatures possibleFeatures : ManeuverFeatures.values()) {
            if (possibleFeatures.isSubset(maneuverFeatures)) {
                ManeuverModelMetadata modelMetadata = createModelMetadata(possibleFeatures, null);
                modelMetadataCandidates.add(modelMetadata);
                if (boatClass != null) {
                    ManeuverModelMetadata modelMetadataWithBoatClass = createModelMetadata(possibleFeatures, boatClass);
                    modelMetadataCandidates.add(modelMetadataWithBoatClass);
                }
            }
        }
        return modelMetadataCandidates;
    }

}
