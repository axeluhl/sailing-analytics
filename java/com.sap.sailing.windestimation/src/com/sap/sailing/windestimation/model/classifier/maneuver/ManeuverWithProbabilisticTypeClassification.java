package com.sap.sailing.windestimation.model.classifier.maneuver;

import com.sap.sailing.windestimation.aggregator.hmm.ProbabilityUtil;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

/**
 * Represents maneuver classifications backed by a classification model.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverWithProbabilisticTypeClassification
        implements Comparable<ManeuverWithProbabilisticTypeClassification> {

    private final double[] likelihoodPerManeuverType;
    private final ManeuverForEstimation maneuver;

    public ManeuverWithProbabilisticTypeClassification(ManeuverForEstimation maneuver,
            double[] likelihoodPerManeuverType) {
        this.maneuver = maneuver;
        for (int i = 0; i < likelihoodPerManeuverType.length; i++) {
            likelihoodPerManeuverType[i] += 0.05;
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

    @Override
    public int compareTo(ManeuverWithProbabilisticTypeClassification o) {
        return maneuver.compareTo(o.maneuver);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("probabilities: ");
        for (final ManeuverTypeForClassification type : ManeuverTypeForClassification.values()) {
            result.append(type);
            result.append(": ");
            result.append(getManeuverTypeLikelihood(type));
            result.append(", ");
        }
        return result.substring(0, result.length()-2);
    }
}
