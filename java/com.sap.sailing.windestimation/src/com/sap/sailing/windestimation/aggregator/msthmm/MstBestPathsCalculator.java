package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.List;

import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;

public interface MstBestPathsCalculator {

    MstGraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator();

    List<GraphLevelInference> getBestNodes(MstManeuverGraphComponents graphComponents);

}
