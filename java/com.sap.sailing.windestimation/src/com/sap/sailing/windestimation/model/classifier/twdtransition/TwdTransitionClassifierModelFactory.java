package com.sap.sailing.windestimation.model.classifier.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.ClassifierModelFactory;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.classifier.smile.LDAClassifier;

public class TwdTransitionClassifierModelFactory
        implements ClassifierModelFactory<TwdTransition, TwdTransitionClassifierModelMetadata> {

    private static TwdTransitionClassifierModelMetadata createModelMetadata(
            ManeuverTypeTransition maneuverTypeTransition) {
        TwdTransitionClassifierModelMetadata modelMetadata = new TwdTransitionClassifierModelMetadata(
                maneuverTypeTransition);
        return modelMetadata;
    }

    @Override
    public TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> getNewModel(
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadata) {
        TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> classificationModel = new LDAClassifier<>(
                contextSpecificModelMetadata);
        return classificationModel;
    }

    @Override
    public List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata>> getAllTrainableModels(
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadata) {
        List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata>> classifiers = new ArrayList<>();
        // classifiers.add(new GradientBoostingManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new LDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new LogisticRegressionManeuverClassifier(maneuverFeatures, boatClass,
        // supportedManeuverTypes));
        // classifiers.add(new NaiveBayesManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        classifiers.add(new LDAClassifier<>(contextSpecificModelMetadata));
        // classifiers.add(new QDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new RandomForestManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new SVMManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        return classifiers;
    }

    @Override
    public List<TwdTransitionClassifierModelMetadata> getAllValidContextSpecificModelMetadataCandidates(
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        List<TwdTransitionClassifierModelMetadata> modelMetadataCandidates = new ArrayList<>();
        for (ManeuverTypeTransition maneuverTypeTransition : ManeuverTypeTransition.values()) {
            modelMetadataCandidates.add(createModelMetadata(maneuverTypeTransition));
        }
        return modelMetadataCandidates;
    }

}
