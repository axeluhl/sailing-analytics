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
        implements ClassifierModelFactory<TwdTransition, TwdTransitionClassifierModelContext> {

    public TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext> getNewModel(
            TwdTransitionClassifierModelContext modelContext) {
        TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext> classificationModel = new LDAClassifier<>(
                modelContext);
        return classificationModel;
    }

    @Override
    public List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext>> getAllTrainableModels(
            TwdTransitionClassifierModelContext modelContext) {
        TwdTransitionClassifierModelContext clonedModelContext = new TwdTransitionClassifierModelContext(
                modelContext.getManeuverTypeTransition());
        List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext>> classifiers = new ArrayList<>();
        classifiers.add(new NeuralNetworkClassifier<>(clonedModelContext));
        classifiers.add(new LogisticRegressionClassifier<>(clonedModelContext));
        classifiers.add(new NaiveBayesClassifier<>(clonedModelContext));
        classifiers.add(new LDAClassifier<>(clonedModelContext));
        classifiers.add(new QDAClassifier<>(clonedModelContext));
        return classifiers;
    }

    @Override
    public List<TwdTransitionClassifierModelContext> getAllValidModelContexts(
            TwdTransitionClassifierModelContext modelContextWithMaxFeatures) {
        List<TwdTransitionClassifierModelContext> modelContextCandidates = new ArrayList<>();
        modelContextCandidates.add(new TwdTransitionClassifierModelContext(
                modelContextWithMaxFeatures.getManeuverTypeTransition()));
        return modelContextCandidates;
    }

    @Override
    public TwdTransitionClassifierModelContext getContextSpecificModelContextWhichModelIsAlwaysPresentAndHasMinimalFeatures() {
        return new TwdTransitionClassifierModelContext(ManeuverTypeTransition.TACK_TACK);
    }

}
