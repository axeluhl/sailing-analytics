package com.sap.sailing.windestimation.classifier.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.ClassifierModelFactory;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.classifier.smile.NeuralNetworkClassifier;
import com.sap.sailing.windestimation.data.TwdTransition;

public class TwdTransitionClassifierModelFactory
        implements ClassifierModelFactory<TwdTransition, TwdTransitionClassifierModelMetadata> {

    private static TwdTransitionClassifierModelMetadata createModelMetadata(BoatClass boatClass) {
        TwdTransitionClassifierModelMetadata modelMetadata = new TwdTransitionClassifierModelMetadata(boatClass);
        return modelMetadata;
    }

    @Override
    public TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> getNewClassifierModel(
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadata) {
        BoatClass boatClass = contextSpecificModelMetadata.getBoatClass();
        TwdTransitionClassifierModelMetadata modelMetadata = createModelMetadata(boatClass);
        TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> classificationModel = new NeuralNetworkClassifier<>(
                modelMetadata);
        return classificationModel;
    }

    @Override
    public List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata>> getAllTrainableClassifierModels(
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadata) {
        List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata>> classifiers = new ArrayList<>();
        // classifiers.add(new GradientBoostingManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new LDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new LogisticRegressionManeuverClassifier(maneuverFeatures, boatClass,
        // supportedManeuverTypes));
        // classifiers.add(new NaiveBayesManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        classifiers.add(new NeuralNetworkClassifier<>(contextSpecificModelMetadata));
        // classifiers.add(new QDAManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new RandomForestManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        // classifiers.add(new SVMManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes));
        return classifiers;
    }

    @Override
    public List<TwdTransitionClassifierModelMetadata> getAllValidContextSpecificModelMetadataCandidates(
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        BoatClass boatClass = contextSpecificModelMetadataWithMaxFeatures.getBoatClass();
        List<TwdTransitionClassifierModelMetadata> modelMetadataCandidates = new ArrayList<>();
        modelMetadataCandidates.add(createModelMetadata(null));
        if (boatClass != null) {
            modelMetadataCandidates.add(createModelMetadata(boatClass));
        }
        return modelMetadataCandidates;
    }

}
