package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForInternalClassification;

public class GraphNode {

    private final WindCourseRange validWindRange;
    private double confidence;
    private final int indexInLevel;
    private final ManeuverTypeForInternalClassification maneuverType;
    private final Tack tackAfter;

    public GraphNode(ManeuverTypeForInternalClassification maneuverType, Tack tackAfter,
            WindCourseRange validWindRange, double confidence, int indexInLevel) {
        this.maneuverType = maneuverType;
        this.tackAfter = tackAfter;
        this.validWindRange = validWindRange;
        this.confidence = confidence;
        this.indexInLevel = indexInLevel;
    }

    public ManeuverTypeForInternalClassification getManeuverType() {
        return maneuverType;
    }

    public WindCourseRange getValidWindRange() {
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
