package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.List;

import com.sap.sailing.windestimation.aggregator.advancedhmm.AdvancedManeuverGraphGenerator.AdvancedManeuverGraphComponents;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;

public interface AdvancedBestPathsCalculator {

    AdvancedGraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator();

    List<GraphLevelInference> getBestNodes(AdvancedManeuverGraphComponents graphComponents);

}
