package com.sap.sailing.windestimation.classifier.maneuver;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

public class ManeuverWithEstimatedType implements Comparable<ManeuverWithEstimatedType> {

    private final ManeuverForEstimation maneuver;
    private final ManeuverTypeForClassification maneuverType;
    private final double confidence;

    public ManeuverWithEstimatedType(ManeuverForEstimation maneuver, ManeuverTypeForClassification maneuverType,
            double confidence) {
        this.maneuver = maneuver;
        this.maneuverType = maneuverType;
        this.confidence = confidence;
    }

    public ManeuverForEstimation getManeuver() {
        return maneuver;
    }

    public ManeuverTypeForClassification getManeuverType() {
        return maneuverType;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public int compareTo(ManeuverWithEstimatedType o) {
        return maneuver.compareTo(o.maneuver);
    }

}
