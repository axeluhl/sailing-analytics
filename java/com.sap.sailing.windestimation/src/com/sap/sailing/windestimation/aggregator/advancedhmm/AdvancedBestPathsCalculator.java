package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.List;

import com.sap.sailing.windestimation.aggregator.advancedhmm.AdvancedManeuverGraphGenerator.AdvancedManeuverGraphComponents;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;

public interface AdvancedBestPathsCalculator {

    GraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator();

    List<GraphLevelInference> getBestNodes(AdvancedManeuverGraphComponents graphComponents);

}
