package com.sap.sailing.windestimation.maneuvergraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Util.Triple;

public class BestPathCalculatorForConfidenceEvaluation extends BestPathsCalculator {

    private static final int MAX_MANEUVERS_TO_CHECK = 10;

    private static File csvFile = new File("confidenceDevelopment.csv");

    private static double[] errorDegreesSumPerManeuverCount = new double[MAX_MANEUVERS_TO_CHECK];
    private static double[] confidenceSumPerManeuverCount = new double[MAX_MANEUVERS_TO_CHECK];
    private static double[] entriesCountPerManeuverCount = new double[MAX_MANEUVERS_TO_CHECK];

    public BestPathCalculatorForConfidenceEvaluation(
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        super(transitionProbabilitiesCalculator);
    }

    @Override
    public void computeBestPathsToNextLevel(GraphLevel nextLevel) {
        super.computeBestPathsToNextLevel(nextLevel);
        List<Triple<GraphLevel, GraphNode, Double>> bestPath = getBestPath(nextLevel);
        List<WindWithConfidence<Void>> windTrack = getWindTrack(bestPath);
        int maneuversCount = windTrack.size();
        if (maneuversCount > 0 && maneuversCount <= MAX_MANEUVERS_TO_CHECK) {
            double errorDegreesSum = 0;
            double confidenceSum = 0;
            GraphLevel currentLevel = nextLevel;
            while (currentLevel.getPreviousLevel() != null) {
                currentLevel = currentLevel.getPreviousLevel();
            }
            for (WindWithConfidence<Void> windWithConfidence : windTrack) {
                Wind wind = windWithConfidence.getObject();
                while (!currentLevel.getManeuver().getManeuverTimePoint().equals(wind.getTimePoint())) {
                    currentLevel = currentLevel.getNextLevel();
                }
                Wind targetWind = ((LabelledManeuverForEstimation) currentLevel.getManeuver()).getWind();
                Bearing diff = wind.getBearing().getDifferenceTo(targetWind.getBearing());
                errorDegreesSum += Math.abs(diff.getDegrees());
                confidenceSum += windWithConfidence.getConfidence();

            }
            double avgConfidence = confidenceSum / maneuversCount;
            double avgErrorDegrees = errorDegreesSum / maneuversCount;
            synchronized (BestPathCalculatorForConfidenceEvaluation.class) {
                confidenceSumPerManeuverCount[maneuversCount - 1] += avgConfidence;
                errorDegreesSumPerManeuverCount[maneuversCount - 1] += avgErrorDegrees;
                entriesCountPerManeuverCount[maneuversCount - 1]++;
            }
        }
    }

    public static void toCsv() throws IOException {
        synchronized (BestPathCalculatorForConfidenceEvaluation.class) {
            try (FileWriter out = new FileWriter(csvFile)) {
                String line = "Number of maneuvers;Avg. confidence; Avg. error in degrees\r\n";
                System.out.println(line);
                out.write(line);
                for (int i = 0; i < MAX_MANEUVERS_TO_CHECK; i++) {
                    if (entriesCountPerManeuverCount[i] > 0) {
                        double avgErrorDegrees = errorDegreesSumPerManeuverCount[i] / entriesCountPerManeuverCount[i];
                        double avgConfidence = confidenceSumPerManeuverCount[i] / entriesCountPerManeuverCount[i];
                        line = (i + 1) + ";" + avgConfidence + ";" + avgErrorDegrees + "\r\n";
                        System.out.println(line);
                        out.write(line);
                    } else {
                        break;
                    }
                }
            }
        }
    }

}
