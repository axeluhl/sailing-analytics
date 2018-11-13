package com.sap.sailing.windestimation.tackoutlierremoval;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.maneuverclassifier.ManeuverWithEstimatedType;

public class OutlierAnalysisResult {

    private final List<ManeuverWithEstimatedType> includedManeuvers = new ArrayList<>();
    private final List<ManeuverWithEstimatedType> excludedManeuvers = new ArrayList<>();
    private double sumOfConfidencesOfIncludedFixes = 0;
    private double sumOfConfidencesOfExcludedFixes = 0;

    public OutlierAnalysisResult() {
    }

    public void addIncludedManeuver(ManeuverWithEstimatedType maneuverToInclude) {
        includedManeuvers.add(maneuverToInclude);
        sumOfConfidencesOfIncludedFixes += maneuverToInclude.getConfidence();
    }

    public void addExcludedManeuver(ManeuverWithEstimatedType maneuverToExclude) {
        excludedManeuvers.add(maneuverToExclude);
        sumOfConfidencesOfExcludedFixes += maneuverToExclude.getConfidence();
    }
    
    public List<ManeuverWithEstimatedType> getIncludedManeuvers() {
        return includedManeuvers;
    }
    
    public List<ManeuverWithEstimatedType> getExcludedManeuvers() {
        return excludedManeuvers;
    }

    public double getFinalConfidence() {
        double finalConfidence = sumOfConfidencesOfIncludedFixes
                / (sumOfConfidencesOfIncludedFixes + sumOfConfidencesOfExcludedFixes);
        return finalConfidence;
    }

}
