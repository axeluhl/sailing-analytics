package com.sap.sailing.windestimation.classifier.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.ClassifierModelFactory;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.classifier.smile.NeuralNetworkClassifier;
import com.sap.sailing.windestimation.data.TwdTransition;

public class TwdTransitionClassifierModelFactory
        implements ClassifierModelFactory<TwdTransition, TwdTransitionModelMetadata> {

    private static TwdTransitionModelMetadata createModelMetadata(BoatClass boatClass) {
        TwdTransitionModelMetadata modelMetadata = new TwdTransitionModelMetadata(boatClass);
        return modelMetadata;
    }

    @Override
    public TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata> getNewClassifierModel(
            TwdTransitionModelMetadata contextSpecificModelMetadata) {
        BoatClass boatClass = contextSpecificModelMetadata.getBoatClass();
        TwdTransitionModelMetadata modelMetadata = createModelMetadata(boatClass);
        TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata> classificationModel = new NeuralNetworkClassifier<>(
                modelMetadata);
        return classificationModel;
    }

    @Override
    public List<TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata>> getAllTrainableClassifierModels(
            TwdTransitionModelMetadata contextSpecificModelMetadata) {
        List<TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata>> classifiers = new ArrayList<>();
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
    public List<TwdTransitionModelMetadata> getAllValidContextSpecificModelMetadataCandidates(
            TwdTransitionModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        BoatClass boatClass = contextSpecificModelMetadataWithMaxFeatures.getBoatClass();
        List<TwdTransitionModelMetadata> modelMetadataCandidates = new ArrayList<>();
        modelMetadataCandidates.add(createModelMetadata(null));
        if (boatClass != null) {
            modelMetadataCandidates.add(createModelMetadata(boatClass));
        }
        return modelMetadataCandidates;
    }

}
