package com.sap.sailing.windestimation.aggregator.advancedhmm;

import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public interface AdvancedGraphNodeTransitionProbabilitiesCalculator extends GraphNodeTransitionProbabilitiesCalculator {

    double getCompoundDistance(ManeuverForEstimation fromManeuver, ManeuverForEstimation toManeuver);

}
