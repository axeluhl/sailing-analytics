package com.sap.sailing.windestimation.model.classifier.twdtransition;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.ClassifierModelFactory;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.classifier.smile.LDAClassifier;
import com.sap.sailing.windestimation.model.classifier.smile.LogisticRegressionClassifier;
import com.sap.sailing.windestimation.model.classifier.smile.NaiveBayesClassifier;
import com.sap.sailing.windestimation.model.classifier.smile.NeuralNetworkClassifier;
import com.sap.sailing.windestimation.model.classifier.smile.QDAClassifier;

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
        classifiers.add(new NeuralNetworkClassifier<>(contextSpecificModelMetadata));
        classifiers.add(new LogisticRegressionClassifier<>(contextSpecificModelMetadata));
        classifiers.add(new NaiveBayesClassifier<>(contextSpecificModelMetadata));
        classifiers.add(new LDAClassifier<>(contextSpecificModelMetadata));
        classifiers.add(new QDAClassifier<>(contextSpecificModelMetadata));
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
