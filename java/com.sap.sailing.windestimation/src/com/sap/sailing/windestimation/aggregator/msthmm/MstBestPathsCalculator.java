package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.List;

import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;

/**
 * Infers best path within MST using an adapted variant of Viterbi for conventional HMM models which allows to label
 * each provided maneuver with its most suitable maneuver type.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface MstBestPathsCalculator {

    MstGraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator();

    List<GraphLevelInference> getBestNodes(MstManeuverGraphComponents graphComponents);

}
