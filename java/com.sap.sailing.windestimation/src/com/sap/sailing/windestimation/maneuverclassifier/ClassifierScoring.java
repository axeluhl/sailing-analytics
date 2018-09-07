package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.List;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.impl.MLUtil;

import smile.validation.ConfusionMatrix;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ClassifierScoring {

    private TrainableSingleManeuverOfflineClassifier trainedClassifier;
    private double lastAvgF1Score = 0;
    private double lastAvgPrecision = 0;
    private double lastAvgRecall = 0;

    public ClassifierScoring(TrainableSingleManeuverOfflineClassifier trainedClassifier) {
        this.trainedClassifier = trainedClassifier;
    }

    public String printScoring(List<ManeuverForEstimation> maneuvers) {
        int[] target = MLUtil.getOutputAsIntArray(maneuvers, trainedClassifier.getSupportedManeuverTypesMapping());
        int[] predicted = new int[maneuvers.size()];
        int i = 0;
        for (ManeuverForEstimation maneuver : maneuvers) {
            double[] classificationResult = trainedClassifier.classifyManeuverWithProbabilities(maneuver);
            predicted[i++] = argmax(classificationResult);
        }
        int[][] confusionMatrix = new ConfusionMatrix(target, predicted).getMatrix();
        int supportedManeuverTypesCount = trainedClassifier.getSupportedManeuverTypesCount();
        double[] precisionPerClass = new double[supportedManeuverTypesCount];
        double[] recallPerClass = new double[supportedManeuverTypesCount];
        double[] f1ScorePerClass = new double[supportedManeuverTypesCount];
        for (i = 0; i < supportedManeuverTypesCount; i++) {
            int tp = confusionMatrix[i][i];
            int fp = 0;
            int fn = 0;
            for (int j = 0; j < supportedManeuverTypesCount; j++) {
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
        lastAvgPrecision = appendStatistic("Precision", precisionPerClass, supportedManeuverTypesCount, str);
        lastAvgRecall = appendStatistic("Recall", recallPerClass, supportedManeuverTypesCount, str);
        lastAvgF1Score = appendStatistic("F1-Score", f1ScorePerClass, supportedManeuverTypesCount, str);
        return str.toString();
    }

    private double appendStatistic(String statisticName, double[] statisticPerClass, int supportedManeuverTypesCount,
            StringBuilder str) {
        str.append(":\n - ");
        str.append(statisticName);
        str.append("\n   | ");
        double statisticValueSum = 0;
        for (int i = 0; i < supportedManeuverTypesCount; i++) {
            str.append(trainedClassifier.getManeuverTypeByMappingIndex(i).toString());
            double statisticValue = statisticPerClass[i];
            statisticValueSum += statisticValue;
            str.append(String.format(" %.03f | ", statisticValue));
        }
        str.append("AVG");
        double avgStatisticValue = statisticValueSum / supportedManeuverTypesCount;
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
