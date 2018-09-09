package com.sap.sailing.windestimation.maneuvergraph.maneuvernode;

import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;

public class ManeuverNode {

    private final WindRangeForManeuverNode validWindRange;
    private double confidence;
    private final int indexInLevel;
    private final ManeuverTypeForClassification maneuverType;
    private final Tack tackAfter;

    public ManeuverNode(ManeuverTypeForClassification maneuverType, Tack tackAfter,
            WindRangeForManeuverNode validWindRange, double confidence, int indexInLevel) {
        this.maneuverType = maneuverType;
        this.tackAfter = tackAfter;
        this.validWindRange = validWindRange;
        this.confidence = confidence;
        this.indexInLevel = indexInLevel;
    }

    public ManeuverTypeForClassification getManeuverType() {
        return maneuverType;
    }

    public WindRangeForManeuverNode getValidWindRange() {
        return validWindRange;
    }

    public double getConfidence() {
        return confidence;
    }

    public int getIndexInLevel() {
        return indexInLevel;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public Tack getTackAfter() {
        return tackAfter;
    }

}
