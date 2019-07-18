package com.sap.sailing.windestimation.evaluation;

import java.text.DecimalFormat;

import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.model.classifier.ConfusionMatrixScoring;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimatorEvaluationResult {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.000");

    private final int numberOfCorrectWindDirectionEstimations;
    private final int numberOfEmptyWindDirectionEstimations;
    private final int numberOfIncorrectWindDirectionEstimations;
    private final int numberOfCorrectWindSpeedEstimations;
    private final int numberOfIncorrectWindSpeedEstimations;
    private final int numberOfCorrectWindDirectionWithSpeedEstimations;
    private final int numberOfIncorrectWindDirectionWithSpeedEstimations;
    private final double sumAbsWindCourseErrorInDegreesOfCorrectEstimations;
    private final double sumAbsWindCourseErrorInDegreesOfIncorrectEstimations;
    private final double sumAbsWindSpeedErrorInKnotsOfCorrectEstimations;
    private final double sumAbsWindSpeedErrorInKnotsOfIncorrectEstimations;
    private final double sumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations;
    private final double sumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations;
    private final double sumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations;
    private final double sumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations;
    private final double sumOfConfidencesOfCorrentWindDirectionEstimations;
    private final double sumOfConfidencesOfIncorrentWindDirectionEstimations;
    private final int[][] confusionMatrix;

    public WindEstimatorEvaluationResult() {
        this(null);
    }

    public WindEstimatorEvaluationResult(int[][] confusionMatrix) {
        this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, confusionMatrix);
    }

    public WindEstimatorEvaluationResult(double windCourseErrorInDegrees, boolean windCourseErrorWithinTolerance,
            double windSpeedErrorInKnots, boolean windSpeedErrorWithinTolerance, double confidence,
            int[][] confusionMatrix) {
        this(windCourseErrorWithinTolerance ? 1 : 0, 0, windCourseErrorWithinTolerance ? 0 : 1,
                windSpeedErrorWithinTolerance ? 1 : 0, windSpeedErrorWithinTolerance ? 0 : 1,
                windCourseErrorWithinTolerance && windSpeedErrorWithinTolerance ? 1 : 0,
                windCourseErrorWithinTolerance && windSpeedErrorWithinTolerance ? 0 : 1,
                windCourseErrorWithinTolerance ? Math.abs(windCourseErrorInDegrees) : 0,
                windCourseErrorWithinTolerance ? 0 : Math.abs(windCourseErrorInDegrees),
                windSpeedErrorWithinTolerance ? Math.abs(windSpeedErrorInKnots) : 0,
                windSpeedErrorWithinTolerance ? 0 : Math.abs(windSpeedErrorInKnots),
                windCourseErrorWithinTolerance && windSpeedErrorWithinTolerance ? Math.abs(windCourseErrorInDegrees)
                        : 0,
                windCourseErrorWithinTolerance && windSpeedErrorWithinTolerance ? 0
                        : Math.abs(windCourseErrorInDegrees),
                windCourseErrorWithinTolerance && windSpeedErrorWithinTolerance ? Math.abs(windSpeedErrorInKnots) : 0,
                windCourseErrorWithinTolerance && windSpeedErrorWithinTolerance ? 0 : Math.abs(windSpeedErrorInKnots),
                windCourseErrorWithinTolerance ? confidence : 0, windCourseErrorWithinTolerance ? 0 : confidence,
                confusionMatrix);
    }

    public WindEstimatorEvaluationResult(double windCourseErrorInDegrees, boolean windCourseErrorWithinTolerance,
            double confidence, int[][] confusionMatrix) {
        this(windCourseErrorWithinTolerance ? 1 : 0, 0, windCourseErrorWithinTolerance ? 0 : 1, 0, 0, 0, 0,
                windCourseErrorWithinTolerance ? Math.abs(windCourseErrorInDegrees) : 0,
                windCourseErrorWithinTolerance ? 0 : Math.abs(windCourseErrorInDegrees), 0, 0, 0, 0, 0, 0,
                windCourseErrorWithinTolerance ? confidence : 0, windCourseErrorWithinTolerance ? 0 : confidence,
                confusionMatrix);
    }

    public WindEstimatorEvaluationResult(int numberOfCorrectWindDirectionEstimations,
            int numberOfEmptyWindDirectionEstimations, int numberOfIncorrectWindDirectionEstimations,
            int numberOfCorrectWindSpeedEstimations, int numberOfIncorrectWindSpeedEstimations,
            int numberOfCorrectWindDirectionWithSpeedEstimations,
            int numberOfIncorrectWindDirectionWithSpeedEstimations,
            double sumAbsWindCourseErrorInDegreesOfCorrectEstimations,
            double sumAbsWindCourseErrorInDegreesOfIncorrectEstimations,
            double sumAbsWindSpeedErrorInKnotsOfCorrectEstimations,
            double sumAbsWindSpeedErrorInKnotsOfIncorrectEstimations,
            double sumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations,
            double sumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations,
            double sumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations,
            double sumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations,
            double sumOfConfidencesOfCorrentWindDirectionEstimations,
            double sumOfConfidencesOfIncorrentWindDirectionEstimations, int[][] confusionMatrix) {
        this.numberOfCorrectWindDirectionEstimations = numberOfCorrectWindDirectionEstimations;
        this.numberOfEmptyWindDirectionEstimations = numberOfEmptyWindDirectionEstimations;
        this.numberOfIncorrectWindDirectionEstimations = numberOfIncorrectWindDirectionEstimations;
        this.numberOfCorrectWindSpeedEstimations = numberOfCorrectWindSpeedEstimations;
        this.numberOfIncorrectWindSpeedEstimations = numberOfIncorrectWindSpeedEstimations;
        this.numberOfCorrectWindDirectionWithSpeedEstimations = numberOfCorrectWindDirectionWithSpeedEstimations;
        this.numberOfIncorrectWindDirectionWithSpeedEstimations = numberOfIncorrectWindDirectionWithSpeedEstimations;
        this.sumAbsWindCourseErrorInDegreesOfCorrectEstimations = sumAbsWindCourseErrorInDegreesOfCorrectEstimations;
        this.sumAbsWindCourseErrorInDegreesOfIncorrectEstimations = sumAbsWindCourseErrorInDegreesOfIncorrectEstimations;
        this.sumAbsWindSpeedErrorInKnotsOfCorrectEstimations = sumAbsWindSpeedErrorInKnotsOfCorrectEstimations;
        this.sumAbsWindSpeedErrorInKnotsOfIncorrectEstimations = sumAbsWindSpeedErrorInKnotsOfIncorrectEstimations;
        this.sumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations = sumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations;
        this.sumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations = sumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations;
        this.sumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations = sumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations;
        this.sumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations = sumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations;
        this.sumOfConfidencesOfCorrentWindDirectionEstimations = sumOfConfidencesOfCorrentWindDirectionEstimations;
        this.sumOfConfidencesOfIncorrentWindDirectionEstimations = sumOfConfidencesOfIncorrentWindDirectionEstimations;
        this.confusionMatrix = confusionMatrix;
    }

    public int getNumberOfCorrectWindDirectionEstimations() {
        return numberOfCorrectWindDirectionEstimations;
    }

    public int getNumberOfIncorrectWindDirectionEstimations() {
        return numberOfIncorrectWindDirectionEstimations;
    }

    public int getNumberOfCorrectWindSpeedEstimations() {
        return numberOfCorrectWindSpeedEstimations;
    }

    public int getNumberOfIncorrectWindSpeedEstimations() {
        return numberOfIncorrectWindSpeedEstimations;
    }

    public int getNumberOfCorrectWindDirectionWithSpeedEstimations() {
        return numberOfCorrectWindDirectionWithSpeedEstimations;
    }

    public int getNumberOfIncorrectWindDirectionWithSpeedEstimations() {
        return numberOfIncorrectWindDirectionWithSpeedEstimations;
    }

    public int getTotalNumberOfWindDirectionEstimations() {
        return numberOfCorrectWindDirectionEstimations + numberOfIncorrectWindDirectionEstimations;
    }

    public int getTotalNumberOfWindSpeedEstimations() {
        return numberOfCorrectWindSpeedEstimations + numberOfIncorrectWindSpeedEstimations;
    }

    public int getTotalNumberOfWindDirectionWithSpeedEstimations() {
        return numberOfCorrectWindDirectionWithSpeedEstimations + numberOfIncorrectWindDirectionWithSpeedEstimations;
    }

    public double getSumAbsWindCourseErrorInDegreesOfCorrectEstimations() {
        return sumAbsWindCourseErrorInDegreesOfCorrectEstimations;
    }

    public double getSumAbsWindSpeedErrorInKnotsOfCorrectEstimations() {
        return sumAbsWindSpeedErrorInKnotsOfCorrectEstimations;
    }

    public double getSumAbsWindCourseErrorInDegreesOfIncorrectEstimations() {
        return sumAbsWindCourseErrorInDegreesOfIncorrectEstimations;
    }

    public double getSumAbsWindSpeedErrorInKnotsOfIncorrectEstimations() {
        return sumAbsWindSpeedErrorInKnotsOfIncorrectEstimations;
    }

    public double getSumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations() {
        return sumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations;
    }

    public double getSumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations() {
        return sumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations;
    }

    public double getSumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations() {
        return sumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations;
    }

    public double getSumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations() {
        return sumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations;
    }

    public double getAccuracyOfWindDirectionEstimation() {
        return nullSafeDivision(numberOfCorrectWindDirectionEstimations,
                numberOfCorrectWindDirectionEstimations + numberOfIncorrectWindDirectionEstimations);
    }

    public double getAccuracyOfWindSpeedEstimation() {
        return nullSafeDivision(numberOfCorrectWindSpeedEstimations,
                numberOfCorrectWindSpeedEstimations + numberOfIncorrectWindSpeedEstimations);
    }

    public double getAccuracyOfWindDirectionWithSpeedEstimation() {
        return nullSafeDivision(numberOfCorrectWindDirectionWithSpeedEstimations,
                numberOfCorrectWindDirectionWithSpeedEstimations + numberOfIncorrectWindDirectionWithSpeedEstimations);
    }

    public double getAvgAbsWindCourseErrorInDegreesOfCorrectWindDirectionEstimations() {
        return nullSafeDivision(sumAbsWindCourseErrorInDegreesOfCorrectEstimations,
                numberOfCorrectWindDirectionEstimations);
    }

    public double getAvgAbsWindCourseErrorInDegreesOfIncorrectWindDirectionEstimations() {
        return nullSafeDivision(sumAbsWindCourseErrorInDegreesOfIncorrectEstimations,
                numberOfIncorrectWindDirectionEstimations);
    }

    public double getAvgAbsWindCourseErrorInDegreesOfCorrectAndIncorrectWindDirectionEstimations() {
        return nullSafeDivision(
                sumAbsWindCourseErrorInDegreesOfCorrectEstimations
                        + sumAbsWindCourseErrorInDegreesOfIncorrectEstimations,
                numberOfCorrectWindDirectionEstimations + numberOfIncorrectWindDirectionEstimations);
    }

    public double getAvgAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations() {
        return nullSafeDivision(sumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations,
                numberOfCorrectWindDirectionWithSpeedEstimations);
    }

    public double getAvgAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations() {
        return nullSafeDivision(sumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations,
                numberOfIncorrectWindDirectionWithSpeedEstimations);
    }

    public double getAvgAbsWindCourseErrorInDegreesOfCorrectAndIncorrectWindDirectionWithSpeedEstimations() {
        return nullSafeDivision(
                sumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations
                        + sumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations,
                numberOfCorrectWindDirectionWithSpeedEstimations + numberOfIncorrectWindDirectionWithSpeedEstimations);
    }

    public double getAvgAbsWindSpeedErrorInKnotsOfCorrectWindSpeedEstimations() {
        return nullSafeDivision(sumAbsWindSpeedErrorInKnotsOfCorrectEstimations, numberOfCorrectWindSpeedEstimations);
    }

    public double getAvgAbsWindSpeedErrorInKnotsOfIncorrectWindSpeedEstimations() {
        return nullSafeDivision(sumAbsWindSpeedErrorInKnotsOfIncorrectEstimations,
                numberOfIncorrectWindSpeedEstimations);
    }

    public double getAvgAbsWindSpeedErrorInKnotsOfCorrectAndIncorrectWindSpeedEstimations() {
        return nullSafeDivision(
                sumAbsWindSpeedErrorInKnotsOfCorrectEstimations + sumAbsWindSpeedErrorInKnotsOfIncorrectEstimations,
                numberOfCorrectWindSpeedEstimations + numberOfIncorrectWindSpeedEstimations);
    }

    public double getAvgAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations() {
        return nullSafeDivision(sumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations,
                numberOfCorrectWindDirectionWithSpeedEstimations);
    }

    public double getAvgAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations() {
        return nullSafeDivision(sumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations,
                numberOfIncorrectWindDirectionWithSpeedEstimations);
    }

    public double getAvgAbsWindSpeedErrorInKnotsOfCorrectAndIncorrectWindDirectionWithSpeedEstimations() {
        return nullSafeDivision(
                sumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations
                        + sumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations,
                numberOfCorrectWindDirectionWithSpeedEstimations + numberOfIncorrectWindDirectionWithSpeedEstimations);
    }

    public double getAvgConfidenceOfCorrectWindDirectionEstimations() {
        return nullSafeDivision(sumOfConfidencesOfCorrentWindDirectionEstimations,
                numberOfCorrectWindDirectionEstimations);
    }

    public double getAvgConfidenceOfIncorrectWindDirectionEstimations() {
        return nullSafeDivision(sumOfConfidencesOfIncorrentWindDirectionEstimations,
                numberOfIncorrectWindDirectionEstimations);
    }

    public double getAvgConfidenceOfCorrectAndIncorrectWindDirectionEstimations() {
        return nullSafeDivision(
                sumOfConfidencesOfCorrentWindDirectionEstimations + sumOfConfidencesOfIncorrentWindDirectionEstimations,
                numberOfCorrectWindDirectionEstimations + numberOfIncorrectWindDirectionEstimations);
    }

    public int getNumberOfEmptyWindDirectionEstimations() {
        return numberOfEmptyWindDirectionEstimations;
    }

    public double getPercentageOfEmptyWindDirectionEstimations() {
        return nullSafeDivision(numberOfEmptyWindDirectionEstimations, numberOfCorrectWindDirectionEstimations
                + numberOfIncorrectWindDirectionEstimations + numberOfEmptyWindDirectionEstimations);
    }

    public int getNumberOfCorrectlyEstimatedManeuverTypes() {
        if (confusionMatrix == null) {
            return 0;
        }
        int tp = 0;
        for (int i = 0; i < confusionMatrix.length; i++) {
            tp += confusionMatrix[i][i];
        }
        return tp;
    }

    public int getNumberOfIncorrectlyEstimatedManeuverTypes() {
        if (confusionMatrix == null) {
            return 0;
        }
        int fp = 0;
        for (int i = 0; i < confusionMatrix.length; i++) {
            for (int j = 0; j < confusionMatrix.length; j++) {
                if (i != j) {
                    fp += confusionMatrix[i][j];
                }
            }
        }
        ;
        return fp;
    }

    public int getNumberOfAllEstimatedManeuverTypes() {
        return getNumberOfCorrectlyEstimatedManeuverTypes() + getNumberOfIncorrectlyEstimatedManeuverTypes();
    }

    public double getPercentageOfCorrectlyEstimatedManeuverTypes() {
        return nullSafeDivision(getNumberOfCorrectlyEstimatedManeuverTypes(), getNumberOfAllEstimatedManeuverTypes());
    }

    public void printEvaluationStatistics(boolean detailed) {
        System.out.println("### Wind direction ###");
        System.out.println(" Accuracy: " + formatPercentage(getAccuracyOfWindDirectionEstimation()) + " ("
                + numberOfCorrectWindDirectionEstimations + "/"
                + (numberOfCorrectWindDirectionEstimations + numberOfIncorrectWindDirectionEstimations) + " correct)");
        if (detailed) {
            System.out.println(" Empty estimations: " + formatPercentage(getPercentageOfEmptyWindDirectionEstimations())
                    + " (" + numberOfEmptyWindDirectionEstimations + "/" + (numberOfCorrectWindDirectionEstimations
                            + numberOfIncorrectWindDirectionEstimations + numberOfEmptyWindDirectionEstimations)
                    + " empty)");
        }
        System.out.println(" Avg. wind course error : "
                + formatDegrees(getAvgAbsWindCourseErrorInDegreesOfCorrectWindDirectionEstimations()));
        System.out.println(" Avg. wind course error of incorrect estimations : "
                + formatDegrees(getAvgAbsWindCourseErrorInDegreesOfIncorrectWindDirectionEstimations()));
        System.out.println(" Avg. wind course error of all estimations : "
                + formatDegrees(getAvgAbsWindCourseErrorInDegreesOfCorrectAndIncorrectWindDirectionEstimations()));
        System.out.println(" Avg. confidence of correct estimations : "
                + formatPercentage(getAvgConfidenceOfCorrectWindDirectionEstimations()));
        System.out.println(" Avg. confidence of incorrect estimations : "
                + formatPercentage(getAvgConfidenceOfIncorrectWindDirectionEstimations()));
        System.out.println(" Avg. confidence of all estimations : "
                + formatPercentage(getAvgConfidenceOfCorrectAndIncorrectWindDirectionEstimations()));
        System.out.println(
                " Correctly estimated maneuvers : " + formatPercentage(getPercentageOfCorrectlyEstimatedManeuverTypes())
                        + " (" + getNumberOfCorrectlyEstimatedManeuverTypes() + "/"
                        + (getNumberOfAllEstimatedManeuverTypes()) + " correct)");
        if (detailed) {
            System.out.println(new ConfusionMatrixScoring("Maneuver type estimation scoring",
                    i -> ManeuverTypeForClassification.values()[i].toString()).printScoring(confusionMatrix));
            System.out.println();
            System.out.println("### Wind speed ###");
            System.out.println(" Accuracy: " + formatPercentage(getAccuracyOfWindSpeedEstimation()) + " ("
                    + numberOfCorrectWindSpeedEstimations + "/"
                    + (numberOfCorrectWindSpeedEstimations + numberOfIncorrectWindSpeedEstimations) + " correct)");
            System.out.println(" Avg. wind speed error : "
                    + formatKnots(getAvgAbsWindSpeedErrorInKnotsOfCorrectWindSpeedEstimations()));
            System.out.println(" Avg. wind speed error of incorrect estimations : "
                    + formatKnots(getAvgAbsWindSpeedErrorInKnotsOfIncorrectWindSpeedEstimations()));
            System.out.println(" Avg. wind speed error of all estimations : "
                    + formatKnots(getAvgAbsWindSpeedErrorInKnotsOfCorrectAndIncorrectWindSpeedEstimations()));
            System.out.println();
            System.out.println("### Wind course and speed ###");
            System.out.println(" Accuracy: " + formatPercentage(getAccuracyOfWindDirectionWithSpeedEstimation()) + " ("
                    + numberOfCorrectWindDirectionWithSpeedEstimations + "/"
                    + (numberOfCorrectWindDirectionWithSpeedEstimations
                            + numberOfIncorrectWindDirectionWithSpeedEstimations)
                    + " correct)");
            System.out.println(" Avg. wind course error : "
                    + formatDegrees(getAvgAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations()));
            System.out.println(" Avg. wind course error of incorrect estimations : "
                    + formatDegrees(getAvgAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations()));
            System.out.println(" Avg. wind course error of all estimations : " + formatDegrees(
                    getAvgAbsWindCourseErrorInDegreesOfCorrectAndIncorrectWindDirectionWithSpeedEstimations()));
            System.out.println(" Avg. wind speed error : "
                    + formatKnots(getAvgAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations()));
            System.out.println(" Avg. wind speed error of incorrect estimations : "
                    + formatKnots(getAvgAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations()));
            System.out.println(" Avg. wind speed error of all estimations : " + formatKnots(
                    getAvgAbsWindSpeedErrorInKnotsOfCorrectAndIncorrectWindDirectionWithSpeedEstimations()));
        }
    }

    private String formatPercentage(double value) {
        return decimalFormat.format(value * 100) + " %";
    }

    private String formatDegrees(double value) {
        return decimalFormat.format(value) + " deg";
    }

    private String formatKnots(double value) {
        return decimalFormat.format(value) + " kn";
    }

    public WindEstimatorEvaluationResult mergeBySum(WindEstimatorEvaluationResult other) {
        return new WindEstimatorEvaluationResult(
                numberOfCorrectWindDirectionEstimations + other.numberOfCorrectWindDirectionEstimations,
                numberOfEmptyWindDirectionEstimations + other.numberOfEmptyWindDirectionEstimations,
                numberOfIncorrectWindDirectionEstimations + other.numberOfIncorrectWindDirectionEstimations,
                numberOfCorrectWindSpeedEstimations + other.numberOfCorrectWindSpeedEstimations,
                numberOfIncorrectWindSpeedEstimations + other.numberOfIncorrectWindSpeedEstimations,
                numberOfCorrectWindDirectionWithSpeedEstimations
                        + other.numberOfCorrectWindDirectionWithSpeedEstimations,
                numberOfIncorrectWindDirectionWithSpeedEstimations
                        + other.numberOfIncorrectWindDirectionWithSpeedEstimations,
                sumAbsWindCourseErrorInDegreesOfCorrectEstimations
                        + other.sumAbsWindCourseErrorInDegreesOfCorrectEstimations,
                sumAbsWindCourseErrorInDegreesOfIncorrectEstimations
                        + other.sumAbsWindCourseErrorInDegreesOfIncorrectEstimations,
                sumAbsWindSpeedErrorInKnotsOfCorrectEstimations + other.sumAbsWindSpeedErrorInKnotsOfCorrectEstimations,
                sumAbsWindSpeedErrorInKnotsOfIncorrectEstimations
                        + other.sumAbsWindSpeedErrorInKnotsOfIncorrectEstimations,
                sumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations
                        + other.sumAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations,
                sumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations
                        + other.sumAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations,
                sumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations
                        + other.sumAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations,
                sumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations
                        + other.sumAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations,
                sumOfConfidencesOfCorrentWindDirectionEstimations
                        + other.sumOfConfidencesOfCorrentWindDirectionEstimations,
                sumOfConfidencesOfIncorrentWindDirectionEstimations
                        + other.sumOfConfidencesOfIncorrentWindDirectionEstimations,
                mergeConfusionMatrix(confusionMatrix, other.confusionMatrix));
    }

    public WindEstimatorEvaluationResult getAvgAsSingleResult(double minAccuracyForCorrectEstimation) {
        if (getTotalNumberOfWindDirectionEstimations() == 0) {
            return new WindEstimatorEvaluationResult(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null);
        }
        if (nullSafeDivision(getTotalNumberOfWindDirectionEstimations() - getTotalNumberOfWindSpeedEstimations(),
                getTotalNumberOfWindDirectionEstimations() + getTotalNumberOfWindSpeedEstimations()) < 0.2) {
            boolean windCourseCorrect = getAccuracyOfWindDirectionEstimation() >= minAccuracyForCorrectEstimation;
            boolean windSpeedCorrect = getAccuracyOfWindSpeedEstimation() >= minAccuracyForCorrectEstimation;
            boolean windCourseAndSpeedCorrect = getAccuracyOfWindDirectionWithSpeedEstimation() >= minAccuracyForCorrectEstimation;
            return new WindEstimatorEvaluationResult(windCourseCorrect ? 1 : 0,
                    getTotalNumberOfWindDirectionEstimations() == 0 ? 1 : 0, windCourseCorrect ? 0 : 1,
                    windSpeedCorrect ? 1 : 0, windSpeedCorrect ? 0 : 1, windCourseAndSpeedCorrect ? 1 : 0,
                    windCourseAndSpeedCorrect ? 0 : 1,
                    windCourseCorrect ? getAvgAbsWindCourseErrorInDegreesOfCorrectWindDirectionEstimations() : 0,
                    windCourseCorrect ? 0 : getAvgAbsWindCourseErrorInDegreesOfIncorrectWindDirectionEstimations(),
                    windSpeedCorrect ? getAvgAbsWindSpeedErrorInKnotsOfCorrectWindSpeedEstimations() : 0,
                    windSpeedCorrect ? 0 : getAvgAbsWindSpeedErrorInKnotsOfIncorrectWindSpeedEstimations(),
                    windCourseAndSpeedCorrect
                            ? getAvgAbsWindCourseErrorInDegreesOfCorrectWindDirectionWithSpeedEstimations()
                            : 0,
                    windCourseAndSpeedCorrect ? 0
                            : getAvgAbsWindCourseErrorInDegreesOfIncorrectWindDirectionWithSpeedEstimations(),
                    windCourseAndSpeedCorrect
                            ? getAvgAbsWindSpeedErrorInKnotsOfCorrectWindDirectionWithSpeedEstimations()
                            : 0,
                    windCourseAndSpeedCorrect ? 0
                            : getAvgAbsWindSpeedErrorInKnotsOfIncorrectWindDirectionWithSpeedEstimations(),
                    windCourseCorrect ? getAvgConfidenceOfCorrectWindDirectionEstimations() : 0,
                    windCourseCorrect ? 0 : getAvgConfidenceOfIncorrectWindDirectionEstimations(), confusionMatrix);
        }
        boolean estimationCorrect = getAccuracyOfWindDirectionEstimation() >= minAccuracyForCorrectEstimation;
        return new WindEstimatorEvaluationResult(estimationCorrect ? 1 : 0,
                getTotalNumberOfWindDirectionEstimations() == 0 ? 1 : 0, estimationCorrect ? 0 : 1, 0, 0, 0, 0,
                estimationCorrect ? getAvgAbsWindCourseErrorInDegreesOfCorrectWindDirectionEstimations() : 0,
                estimationCorrect ? 0 : getAvgAbsWindCourseErrorInDegreesOfIncorrectWindDirectionEstimations(), 0, 0, 0,
                0, 0, 0, estimationCorrect ? getAvgConfidenceOfCorrectWindDirectionEstimations() : 0,
                estimationCorrect ? 0 : getAvgConfidenceOfIncorrectWindDirectionEstimations(), confusionMatrix);
    }

    private double nullSafeDivision(double dividend, double divisor) {
        if (divisor == 0) {
            return 0;
        }
        return dividend / divisor;
    }

    private static int[][] mergeConfusionMatrix(int[][] oneConfusionMatrix, int[][] otherConfusionMatrix) {
        if (oneConfusionMatrix == null) {
            return otherConfusionMatrix;
        }
        if (otherConfusionMatrix == null) {
            return oneConfusionMatrix;
        }
        int[][] newConfusionMatrix = new int[oneConfusionMatrix.length][oneConfusionMatrix[0].length];
        for (int i = 0; i < newConfusionMatrix.length; i++) {
            for (int j = 0; j < newConfusionMatrix.length; j++) {
                newConfusionMatrix[i][j] = oneConfusionMatrix[i][j] + otherConfusionMatrix[i][j];
            }
        }
        return newConfusionMatrix;
    }

}
