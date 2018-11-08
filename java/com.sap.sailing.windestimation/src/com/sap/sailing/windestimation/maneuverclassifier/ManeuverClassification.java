package com.sap.sailing.windestimation.maneuverclassifier;

import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuvergraph.ProbabilityUtil;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverClassification {

    private final double[] likelihoodPerManeuverType;
    private final ManeuverForEstimation maneuver;

    public ManeuverClassification(ManeuverForEstimation maneuver, double[] likelihoodPerManeuverType) {
        this.maneuver = maneuver;
        for (int i = 0; i < likelihoodPerManeuverType.length; i++) {
            likelihoodPerManeuverType[i] += 0.1;
        }
        ProbabilityUtil.normalizeLikelihoodArray(likelihoodPerManeuverType);
        this.likelihoodPerManeuverType = likelihoodPerManeuverType;
    }

    public ManeuverForEstimation getManeuver() {
        return maneuver;
    }

    public double getManeuverTypeLikelihood(ManeuverTypeForClassification maneuverType) {
        return likelihoodPerManeuverType[maneuverType.ordinal()];
    }

}
