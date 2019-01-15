package com.sap.sailing.windestimation.aggregator.msthmm;

import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public interface MstGraphNodeTransitionProbabilitiesCalculator extends GraphNodeTransitionProbabilitiesCalculator {

    double getCompoundDistance(ManeuverForEstimation fromManeuver, ManeuverForEstimation toManeuver);

}
