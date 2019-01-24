package com.sap.sailing.windestimation.model.classifier.maneuver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.model.classifier.ClassifierModelFactory;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.classifier.smile.NeuralNetworkClassifier;

public class ManeuverClassifierModelFactory
        implements ClassifierModelFactory<ManeuverForEstimation, ManeuverClassifierModelMetadata> {

    public static final ManeuverTypeForClassification[] orderedSupportedTargetValues = {
            ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.JIBE };

    @Override
    public TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata> getNewModel(
            ManeuverClassifierModelMetadata contextSpecificModelMetadata) {
        TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata> classificationModel = new NeuralNetworkClassifier<>(
                contextSpecificModelMetadata);
        return classificationModel;
    }

    @Override
    public List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata>> getAllTrainableModels(
            ManeuverClassifierModelMetadata contextSpecificModelMetadata) {
        ManeuverClassifierModelMetadata modelMetadata = createModelMetadata(
                contextSpecificModelMetadata.getManeuverFeatures(), contextSpecificModelMetadata.getBoatClassName());
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata>> classifiers = new ArrayList<>();
        classifiers.add(new NeuralNetworkClassifier<>(modelMetadata));
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata>> suitableClassifiers = classifiers
                .stream().filter(classifier -> classifier.hasSupportForProvidedFeatures()).collect(Collectors.toList());
        return suitableClassifiers;
    }

    private static ManeuverClassifierModelMetadata createModelMetadata(ManeuverFeatures maneuverFeatures,
            String boatClassName) {
        ManeuverClassifierModelMetadata modelMetadata = new ManeuverClassifierModelMetadata(maneuverFeatures,
                boatClassName, orderedSupportedTargetValues);
        return modelMetadata;
    }

    @Override
    public List<ManeuverClassifierModelMetadata> getAllValidContextSpecificModelMetadataFeatureSupersets(
            ManeuverClassifierModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        ManeuverFeatures maneuverFeatures = contextSpecificModelMetadataWithMaxFeatures.getManeuverFeatures();
        String boatClassName = contextSpecificModelMetadataWithMaxFeatures.getBoatClassName();
        List<ManeuverClassifierModelMetadata> modelMetadataCandidates = new ArrayList<>();
        for (ManeuverFeatures possibleFeatures : ManeuverFeatures.values()) {
            if (possibleFeatures.isSubset(maneuverFeatures)) {
                ManeuverClassifierModelMetadata modelMetadata = createModelMetadata(possibleFeatures, null);
                modelMetadataCandidates.add(modelMetadata);
                if (boatClassName != null) {
                    ManeuverClassifierModelMetadata modelMetadataWithBoatClass = createModelMetadata(possibleFeatures,
                            boatClassName);
                    modelMetadataCandidates.add(modelMetadataWithBoatClass);
                }
            }
        }
        return modelMetadataCandidates;
    }

    @Override
    public ManeuverClassifierModelMetadata getContextSpecificModelMetadataWhichModelIsAlwaysPresentAndHasMinimalFeatures() {
        return new ManeuverClassifierModelMetadata(new ManeuverFeatures(false, false, false), null,
                ManeuverClassifierModelFactory.orderedSupportedTargetValues);
    }

}
