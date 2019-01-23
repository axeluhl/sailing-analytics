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

    public TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> getNewModel(
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadata) {
        TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> classificationModel = new LDAClassifier<>(
                contextSpecificModelMetadata);
        return classificationModel;
    }

    @Override
    public List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata>> getAllTrainableModels(
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadata) {
        TwdTransitionClassifierModelMetadata modelMetadata = new TwdTransitionClassifierModelMetadata(
                contextSpecificModelMetadata.getManeuverTypeTransition());
        List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata>> classifiers = new ArrayList<>();
        classifiers.add(new NeuralNetworkClassifier<>(modelMetadata));
        classifiers.add(new LogisticRegressionClassifier<>(modelMetadata));
        classifiers.add(new NaiveBayesClassifier<>(modelMetadata));
        classifiers.add(new LDAClassifier<>(modelMetadata));
        classifiers.add(new QDAClassifier<>(modelMetadata));
        return classifiers;
    }

    @Override
    public List<TwdTransitionClassifierModelMetadata> getAllValidContextSpecificModelMetadataFeatureSupersets(
            TwdTransitionClassifierModelMetadata contextSpecificModelMetadataWithMaxFeatures) {
        List<TwdTransitionClassifierModelMetadata> modelMetadataCandidates = new ArrayList<>();
        modelMetadataCandidates.add(new TwdTransitionClassifierModelMetadata(
                contextSpecificModelMetadataWithMaxFeatures.getManeuverTypeTransition()));
        return modelMetadataCandidates;
    }

    @Override
    public List<TwdTransitionClassifierModelMetadata> getAllValidContextSpecificModelMetadata() {
        List<TwdTransitionClassifierModelMetadata> result = new ArrayList<>();
        for (ManeuverTypeTransition maneuverTypeTransition : ManeuverTypeTransition.values()) {
            TwdTransitionClassifierModelMetadata modelMetadata = new TwdTransitionClassifierModelMetadata(
                    maneuverTypeTransition);
            result.add(modelMetadata);
        }
        return result;
    }

}
