package com.sap.sailing.windestimation.model.classifier;

import java.util.List;
import java.util.function.Function;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

import smile.validation.ConfusionMatrix;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ClassifierScoring<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends ConfusionMatrixScoring {

    private TrainableClassificationModel<InstanceType, T> trainedClassifier;

    public ClassifierScoring(TrainableClassificationModel<InstanceType, T> trainedClassifierModel,
            Function<Integer, String> indexOfTargetValueToLabelMapper) {
        super(trainedClassifierModel.getClass().getSimpleName(), indexOfTargetValueToLabelMapper);
        this.trainedClassifier = trainedClassifierModel;
    }

    public String printScoring(double[][] x, int[] y) {
        int[] predicted = new int[y.length];
        for (int i = 0; i < x.length; i++) {
            double[] xi = x[i];
            double[] classificationResult = trainedClassifier.classifyWithProbabilities(xi);
            predicted[i] = argmax(classificationResult);
        }
        int[][] confusionMatrix = new ConfusionMatrix(y, predicted).getMatrix();
        return printScoring(confusionMatrix);
    }

    public String printScoring(List<InstanceType> instances, LabelExtraction<InstanceType> labelExtraction) {
        T modelMetadata = trainedClassifier.getContextSpecificModelMetadata();
        double[][] x = new double[instances.size()][];
        int[] y = labelExtraction.getYVector(instances);
        int i = 0;
        for (InstanceType instance : instances) {
            x[i++] = modelMetadata.getX(instance);
        }
        return printScoring(x, y);
    }

    private int argmax(double[] values) {
        int bestI = 0;
        double maxValue = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > maxValue) {
                bestI = i;
                maxValue = values[i];
            }

        }
        return bestI;
    }

}
