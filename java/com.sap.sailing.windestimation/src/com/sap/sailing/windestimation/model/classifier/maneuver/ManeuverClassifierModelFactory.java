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
        implements ClassifierModelFactory<ManeuverForEstimation, ManeuverClassifierModelContext> {

    public static final ManeuverTypeForClassification[] orderedSupportedTargetValues = {
            ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.JIBE };

    @Override
    public TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> getNewModel(
            ManeuverClassifierModelContext modelContext) {
        TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classificationModel = new NeuralNetworkClassifier<>(
                modelContext);
        return classificationModel;
    }

    @Override
    public List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext>> getAllTrainableModels(
            ManeuverClassifierModelContext modelContext) {
        ManeuverClassifierModelContext clonedModelContext = createModelContext(modelContext.getManeuverFeatures(),
                modelContext.getBoatClassName());
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext>> classifiers = new ArrayList<>();
        classifiers.add(new NeuralNetworkClassifier<>(clonedModelContext));
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext>> suitableClassifiers = classifiers
                .stream().filter(classifier -> classifier.hasSupportForProvidedFeatures()).collect(Collectors.toList());
        return suitableClassifiers;
    }

    private static ManeuverClassifierModelContext createModelContext(ManeuverFeatures maneuverFeatures,
            String boatClassName) {
        ManeuverClassifierModelContext modelContext = new ManeuverClassifierModelContext(maneuverFeatures,
                boatClassName, orderedSupportedTargetValues);
        return modelContext;
    }

    @Override
    public List<ManeuverClassifierModelContext> getAllValidModelContexts(
            ManeuverClassifierModelContext modelContextWithMaxFeatures) {
        ManeuverFeatures maneuverFeatures = modelContextWithMaxFeatures.getManeuverFeatures();
        String boatClassName = modelContextWithMaxFeatures.getBoatClassName();
        List<ManeuverClassifierModelContext> modelContextCandidates = new ArrayList<>();
        for (ManeuverFeatures possibleFeatures : ManeuverFeatures.values()) {
            if (possibleFeatures.isSubset(maneuverFeatures)) {
                ManeuverClassifierModelContext modelContext = createModelContext(possibleFeatures, null);
                modelContextCandidates.add(modelContext);
                if (boatClassName != null) {
                    ManeuverClassifierModelContext modelContextWithBoatClass = createModelContext(possibleFeatures,
                            boatClassName);
                    modelContextCandidates.add(modelContextWithBoatClass);
                }
            }
        }
        return modelContextCandidates;
    }

    @Override
    public ManeuverClassifierModelContext getContextSpecificModelContextWhichModelIsAlwaysPresentAndHasMinimalFeatures() {
        return new ManeuverClassifierModelContext(new ManeuverFeatures(false, false, false), null,
                ManeuverClassifierModelFactory.orderedSupportedTargetValues);
    }

}
