package com.sap.sailing.windestimation.model.classifier.maneuver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.model.classifier.ClassifierModelFactory;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.classifier.smile.NeuralNetworkClassifier;

public class ManeuverClassifierModelFactory
        implements ClassifierModelFactory<ManeuverForEstimation, ManeuverClassifierModelMetadata> {

    public static final ManeuverTypeForClassification[] orderedSupportedTargetValues = {
            ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.JIBE };
    private final PolarDataService polarService;

    public ManeuverClassifierModelFactory(PolarDataService polarService) {
        this.polarService = polarService;
    }

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
                contextSpecificModelMetadata.getManeuverFeatures(), contextSpecificModelMetadata.getBoatClass());
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata>> classifiers = new ArrayList<>();
        classifiers.add(new NeuralNetworkClassifier<>(modelMetadata));
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata>> suitableClassifiers = classifiers
                .stream().filter(classifier -> classifier.hasSupportForProvidedFeatures()).collect(Collectors.toList());
        return suitableClassifiers;
    }

    private static ManeuverClassifierModelMetadata createModelMetadata(ManeuverFeatures maneuverFeatures,
            BoatClass boatClass) {
        ManeuverClassifierModelMetadata modelMetadata = new ManeuverClassifierModelMetadata(maneuverFeatures, boatClass,
                orderedSupportedTargetValues);
        return modelMetadata;
    }

    @Override
    public List<ManeuverClassifierModelMetadata> getAllValidContextSpecificModelMetadataFeatureSupersets(
            ManeuverClassifierModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        ManeuverFeatures maneuverFeatures = contextSpecificModelMetadataWithMaxFeatures.getManeuverFeatures();
        BoatClass boatClass = contextSpecificModelMetadataWithMaxFeatures.getBoatClass();
        List<ManeuverClassifierModelMetadata> modelMetadataCandidates = new ArrayList<>();
        for (ManeuverFeatures possibleFeatures : ManeuverFeatures.values()) {
            if (possibleFeatures.isSubset(maneuverFeatures)) {
                ManeuverClassifierModelMetadata modelMetadata = createModelMetadata(possibleFeatures, null);
                modelMetadataCandidates.add(modelMetadata);
                if (boatClass != null) {
                    ManeuverClassifierModelMetadata modelMetadataWithBoatClass = createModelMetadata(possibleFeatures,
                            boatClass);
                    modelMetadataCandidates.add(modelMetadataWithBoatClass);
                }
            }
        }
        return modelMetadataCandidates;
    }

    @Override
    public List<ManeuverClassifierModelMetadata> getAllValidContextSpecificModelMetadata() {
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();
        List<ManeuverClassifierModelMetadata> modelMetadataCandidates = new ArrayList<>();
        for (ManeuverFeatures possibleFeatures : ManeuverFeatures.values()) {
            ManeuverClassifierModelMetadata modelMetadata = createModelMetadata(possibleFeatures, null);
            modelMetadataCandidates.add(modelMetadata);
            for (BoatClass boatClass : allBoatClasses) {
                modelMetadata = createModelMetadata(possibleFeatures, boatClass);
                modelMetadataCandidates.add(modelMetadata);
            }
        }
        return modelMetadataCandidates;
    }

}
