package com.sap.sailing.windestimation.maneuvergraph;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ProbabilityUtil {

    private ProbabilityUtil() {
    }

    public static void normalizeLikelihoodArray(double[] likelihoodsForPointOfSailAfterManeuver) {
        normalizeLikelihoodArray(likelihoodsForPointOfSailAfterManeuver, 0);
    }

    public static void normalizeLikelihoodArray(double[] likelihoodsForPointOfSailAfterManeuver, double laplace) {
        double likelihoodSum = 0;
        for (double likelihood : likelihoodsForPointOfSailAfterManeuver) {
            likelihoodSum += likelihood;
        }
        for (int i = 0; i < likelihoodsForPointOfSailAfterManeuver.length; i++) {
            likelihoodsForPointOfSailAfterManeuver[i] = (likelihoodsForPointOfSailAfterManeuver[i] + laplace)
                    / likelihoodSum;
        }
    }

}
