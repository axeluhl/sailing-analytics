package com.sap.sailing.windestimation.model.classifier;

import java.util.Arrays;
import java.util.function.Function;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ConfusionMatrixScoring {

    private String title;
    private double lastAvgF1Score = 0;
    private double lastAvgPrecision = 0;
    private double lastAvgRecall = 0;
    private final Function<Integer, String> indexOfTargetValueToLabelMapper;

    public ConfusionMatrixScoring(String title, Function<Integer, String> indexOfTargetValueToLabelMapper) {
        this.title = title;
        this.indexOfTargetValueToLabelMapper = indexOfTargetValueToLabelMapper;
    }

    public String printScoring(int[][] confusionMatrix) {
        int supportedTargetValuesCount = confusionMatrix.length;
        double[] precisionPerClass = new double[supportedTargetValuesCount];
        double[] recallPerClass = new double[supportedTargetValuesCount];
        double[] f1ScorePerClass = new double[supportedTargetValuesCount];
        for (int i = 0; i < supportedTargetValuesCount; i++) {
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
        StringBuilder str = new StringBuilder(title);
        lastAvgPrecision = appendStatistic("Precision", precisionPerClass, str);
        lastAvgRecall = appendStatistic("Recall", recallPerClass, str);
        lastAvgF1Score = appendStatistic("F1-Score", f1ScorePerClass, str);
        str.append("\nConfusion matrix\n");
        str.append(Arrays.deepToString(confusionMatrix));
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
