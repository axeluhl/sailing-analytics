package com.sap.sailing.windestimation.model.classifier;

import java.util.List;
import java.util.function.Function;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;

import smile.validation.ConfusionMatrix;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ClassifierScoring<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> {

    private TrainableClassificationModel<InstanceType, T> trainedClassifier;
    private double lastAvgF1Score = 0;
    private double lastAvgPrecision = 0;
    private double lastAvgRecall = 0;
    private final Function<Integer, String> indexOfTargetValueToLabelMapper;

    public ClassifierScoring(TrainableClassificationModel<InstanceType, T> trainedClassifierModel,
            Function<Integer, String> indexOfTargetValueToLabelMapper) {
        this.trainedClassifier = trainedClassifierModel;
        this.indexOfTargetValueToLabelMapper = indexOfTargetValueToLabelMapper;
    }

    public String printScoring(List<InstanceType> instances, LabelExtraction<InstanceType> labelExtraction) {
        T modelMetadata = trainedClassifier.getContextSpecificModelMetadata();
        int[] target = labelExtraction.getYVector(instances);
        int[] predicted = new int[instances.size()];
        int i = 0;
        for (InstanceType instance : instances) {
            double[] x = modelMetadata.getX(instance);
            double[] classificationResult = trainedClassifier.classifyWithProbabilities(x);
            predicted[i++] = argmax(classificationResult);
        }
        int[][] confusionMatrix = new ConfusionMatrix(target, predicted).getMatrix();
        int supportedTargetValuesCount = modelMetadata.getNumberOfPossibleTargetValues();
        double[] precisionPerClass = new double[supportedTargetValuesCount];
        double[] recallPerClass = new double[supportedTargetValuesCount];
        double[] f1ScorePerClass = new double[supportedTargetValuesCount];
        for (i = 0; i < supportedTargetValuesCount; i++) {
            int tp = confusionMatrix[i][i];
            int fp = 0;
            int fn = 0;
            for (int j = 0; j < supportedTargetValuesCount; j++) {
                if (i != j) {
                    fp += confusionMatrix[i][j];
                    fn += confusionMatrix[j][i];
                }
            }
            precisionPerClass[i] = precision(tp, fp);
            recallPerClass[i] = recall(tp, fn);
            f1ScorePerClass[i] = f1score(tp, fp, fn);
        }
        StringBuilder str = new StringBuilder(trainedClassifier.getClass().getName());
        lastAvgPrecision = appendStatistic("Precision", precisionPerClass, str);
        lastAvgRecall = appendStatistic("Recall", recallPerClass, str);
        lastAvgF1Score = appendStatistic("F1-Score", f1ScorePerClass, str);
        return str.toString();
    }

    private double appendStatistic(String statisticName, double[] statisticPerClass, StringBuilder str) {
        str.append(":\n - ");
        str.append(statisticName);
        str.append("\n   | ");
        double statisticValueSum = 0;
        for (int i = 0; i < statisticPerClass.length; i++) {
            str.append(indexOfTargetValueToLabelMapper.apply(i));
            double statisticValue = statisticPerClass[i];
            statisticValueSum += statisticValue;
            str.append(String.format(" %.03f | ", statisticValue));
        }
        str.append("AVG");
        double avgStatisticValue = statisticValueSum / statisticPerClass.length;
        str.append(String.format(" %.03f | ", avgStatisticValue));
        return avgStatisticValue;
    }

    private double recall(int tp, int fn) {
        if (tp + fn == 0) {
            return 0;
        }
        return 1.0 * tp / (tp + fn);
    }

    private double precision(int tp, int fp) {
        if (tp + fp == 0) {
            return 0;
        }
        return 1.0 * tp / (tp + fp);
    }

    private double f1score(int tp, int fp, int fn) {
        double p = precision(tp, fp);
        double r = recall(tp, fn);
        if (p + r == 0) {
            return 0;
        }
        return 2.0 * (p * r) / (p + r);
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

    public double getLastAvgF1Score() {
        return lastAvgF1Score;
    }

    public double getLastAvgPrecision() {
        return lastAvgPrecision;
    }

    public double getLastAvgRecall() {
        return lastAvgRecall;
    }

}
